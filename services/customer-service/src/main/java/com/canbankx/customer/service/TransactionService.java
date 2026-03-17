package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.InboundTransferRequest;
import com.canbankx.customer.dto.InboundTransferResponse;
import com.canbankx.customer.infrastructure.CentralBankClient;
import com.canbankx.customer.infrastructure.InterBankClient;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.CustomerRepository;
import com.canbankx.customer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final InterBankClient interBankClient;
    private final CentralBankClient centralBankClient;

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount) {
        log.info("Processing deposit: {}, amount: {}", accountId, amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new TransactionException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .targetAccountId(accountId)
                .amount(amount)
                .currency("CAD")
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.SETTLED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .settledAt(Instant.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(UUID accountId, BigDecimal amount) {
        log.info("Processing withdraw: {}, amount: {}", accountId, amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new TransactionException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(accountId)
                .amount(amount)
                .currency("CAD")
                .type(Transaction.TransactionType.WITHDRAW)
                .status(Transaction.TransactionStatus.SETTLED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .settledAt(Instant.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transferLocal(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, String idempotencyKey) {
        log.info("Processing local transfer: {} -> {}, amount: {}", sourceAccountId, targetAccountId, amount);

        if (idempotencyKey != null) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent request detected, returning existing transaction: {}", existing.get().getId());
                return existing.get();
            }
        }

        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new TransactionException("Source account not found"));

        Account targetAccount = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new TransactionException("Target account not found"));

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source account");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        targetAccount.setBalance(targetAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .senderBankId(2)
                .receiverBankId(2)
                .amount(amount)
                .currency("CAD")
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SETTLED)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .settledAt(Instant.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Local transfer completed: {}", saved.getId());

        return saved;
    }

    @Transactional
    public Transaction transferInterbank(
            UUID sourceAccountId,
            String targetCustomerEmail,
            BigDecimal amount,
            Integer receiverBankId,
            String idempotencyKey) {

        log.info("Processing interbank transfer: {} -> {} (Bank {}), amount: {}",
                sourceAccountId, targetCustomerEmail, receiverBankId, amount);

        if (idempotencyKey != null) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent request detected, returning existing transaction: {}", existing.get().getId());
                return existing.get();
            }
        }

        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new TransactionException("Source account not found"));

        Customer sender = customerRepository.findById(sourceAccount.getCustomerId())
                .orElseThrow(() -> new TransactionException("Sender customer not found"));

        if (!sender.getKycStatus().equals(Customer.KycStatus.VERIFIED)) {
            throw new TransactionException("Sender KYC not verified");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        accountRepository.save(sourceAccount);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(sourceAccountId)
                .sourceCustomerEmail(sender.getEmail())
                .targetCustomerEmail(targetCustomerEmail)
                .senderBankId(2)
                .receiverBankId(receiverBankId)
                .amount(amount)
                .currency("CAD")
                .type(Transaction.TransactionType.INTERBANK_SEND)
                .status(Transaction.TransactionStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .externalTransactionId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Interbank transfer PENDING: {}", savedTransaction.getId());

        try {
            InboundTransferRequest inboundRequest = InboundTransferRequest.builder()
                    .externalTransactionId(savedTransaction.getExternalTransactionId())
                    .senderCustomerEmail(sender.getEmail())
                    .recipientEmail(targetCustomerEmail)
                    .amount(amount)
                    .senderBankId(2)
                    .currency("CAD")
                    .build();

            InboundTransferResponse response = interBankClient.sendTransferToBank(receiverBankId, inboundRequest);

            if ("ACCEPTED".equals(response.getStatus())) {
                savedTransaction.setStatus(Transaction.TransactionStatus.PROCESSING);
                log.info("Receiver bank accepted transfer: {}", savedTransaction.getId());
            } else {
                savedTransaction.setStatus(Transaction.TransactionStatus.REJECTED);
                sourceAccount.setBalance(sourceAccount.getBalance().add(amount));
                accountRepository.save(sourceAccount);
                log.warn("Receiver bank rejected transfer, refunding sender: {}", savedTransaction.getId());
            }

            transactionRepository.save(savedTransaction);

        } catch (InterBankClient.InterBankCommunicationException e) {
            log.error("Failed to communicate with receiver bank, marking PENDING: {}", savedTransaction.getId(), e);
        }

        return savedTransaction;
    }

    @Transactional
    public InboundTransferResponse receiveInterbankTransfer(InboundTransferRequest request) {
        log.info("Receiving interbank transfer from Bank {} for: {}",
                request.getSenderBankId(), request.getRecipientEmail());

        try {
            Optional<Customer> recipientOpt = customerRepository.findByEmail(request.getRecipientEmail());

            if (recipientOpt.isEmpty()) {
                log.warn("Recipient not found: {}", request.getRecipientEmail());
                return InboundTransferResponse.builder()
                        .externalTransactionId(request.getExternalTransactionId())
                        .status("REJECTED")
                        .reason("Recipient customer not found")
                        .processedAt(Instant.now())
                        .build();
            }

            Customer recipient = recipientOpt.get();

            List<Account> accounts = accountRepository.findByCustomerId(recipient.getId());
            if (accounts.isEmpty()) {
                log.warn("Recipient has no accounts: {}", recipient.getEmail());
                return InboundTransferResponse.builder()
                        .externalTransactionId(request.getExternalTransactionId())
                        .status("REJECTED")
                        .reason("Recipient has no active accounts")
                        .processedAt(Instant.now())
                        .build();
            }

            Account recipientAccount = accounts.get(0);

            recipientAccount.setBalance(recipientAccount.getBalance().add(request.getAmount()));
            accountRepository.save(recipientAccount);

            Transaction transaction = Transaction.builder()
                    .targetAccountId(recipientAccount.getId())
                    .sourceCustomerEmail(request.getSenderCustomerEmail())
                    .targetCustomerEmail(request.getRecipientEmail())
                    .senderBankId(request.getSenderBankId())
                    .receiverBankId(2)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .type(Transaction.TransactionType.INTERBANK_RECEIVE)
                    .status(Transaction.TransactionStatus.SETTLED)
                    .externalTransactionId(request.getExternalTransactionId())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .settledAt(Instant.now())
                    .build();

            transactionRepository.save(transaction);

            log.info("Interbank transfer received and credited: {}", request.getExternalTransactionId());

            return InboundTransferResponse.builder()
                    .externalTransactionId(request.getExternalTransactionId())
                    .status("ACCEPTED")
                    .reason("Transfer credited successfully")
                    .processedAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Error processing inbound transfer", e);
            return InboundTransferResponse.builder()
                    .externalTransactionId(request.getExternalTransactionId())
                    .status("REJECTED")
                    .reason("Internal error: " + e.getMessage())
                    .processedAt(Instant.now())
                    .build();
        }
    }

    @Transactional
    public void processSettlementNotification(String externalTransactionId, String result, String reason) {
        log.info("Processing settlement notification: {} -> {}", externalTransactionId, result);

        Optional<Transaction> transactionOpt = transactionRepository.findByExternalTransactionId(externalTransactionId);

        if (transactionOpt.isEmpty()) {
            log.warn("Settlement notification for unknown transaction: {}", externalTransactionId);
            return;
        }

        Transaction transaction = transactionOpt.get();

        switch (result) {
            case "SETTLED" -> {
                transaction.setStatus(Transaction.TransactionStatus.SETTLED);
                transaction.setSettledAt(Instant.now());
                log.info("Transaction SETTLED: {}", externalTransactionId);
            }
            case "REJECTED" -> {
                if (transaction.getSourceAccountId() != null) {
                    Account sourceAccount = accountRepository.findById(transaction.getSourceAccountId())
                            .orElseThrow(() -> new TransactionException("Source account not found for refund"));

                    sourceAccount.setBalance(sourceAccount.getBalance().add(transaction.getAmount()));
                    accountRepository.save(sourceAccount);
                }
                transaction.setStatus(Transaction.TransactionStatus.REFUNDED);
                log.warn("Transaction REJECTED, refunding sender: {}", externalTransactionId);
            }
            case "EXPIRED" -> {
                if (transaction.getSourceAccountId() != null) {
                    Account sourceAccount = accountRepository.findById(transaction.getSourceAccountId())
                            .orElseThrow(() -> new TransactionException("Source account not found for refund"));

                    sourceAccount.setBalance(sourceAccount.getBalance().add(transaction.getAmount()));
                    accountRepository.save(sourceAccount);
                }
                transaction.setStatus(Transaction.TransactionStatus.EXPIRED);
                log.warn("Transaction EXPIRED, refunding sender: {}", externalTransactionId);
            }
        }

        transaction.setUpdatedAt(Instant.now());
        transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(UUID accountId) {
        return transactionRepository.findBySourceAccountIdOrTargetAccountId(accountId, accountId);
    }

    public Optional<Transaction> getTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public static class TransactionException extends RuntimeException {
        public TransactionException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}
