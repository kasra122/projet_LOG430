package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.CentralBankTransferRequest;
import com.canbankx.customer.dto.CentralBankTransferResponse;
import com.canbankx.customer.infrastructure.CentralBankClient;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CentralBankClient centralBankClient;

    @Transactional
    public Transaction initiateInterbankTransfer(
            UUID senderAccountId,
            String senderEmail,
            String recipientEmail,
            Integer receiverBankId,
            BigDecimal amount,
            String currency) {
        
        log.info("Initiating inter-bank transfer: from {} to {} at bank {}, amount: {}", 
                senderEmail, recipientEmail, receiverBankId, amount);

        Optional<Account> senderAccountOpt = accountRepository.findById(senderAccountId);
        if (senderAccountOpt.isEmpty()) {
            throw new TransactionException("Sender account not found: " + senderAccountId);
        }

        Account senderAccount = senderAccountOpt.get();
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new TransactionException("Insufficient balance. Available: " + senderAccount.getBalance() + 
                    ", Required: " + amount);
        }

        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        accountRepository.save(senderAccount);
        log.info("Deducted {} from sender account {}", amount, senderAccountId);

        String externalTxnId = generateTransactionId();
        String idempotencyKey = generateIdempotencyKey();
        
        Transaction transaction = Transaction.builder()
                .externalTransactionId(externalTxnId)
                .sourceAccountId(senderAccountId)
                .sourceCustomerEmail(senderEmail)
                .targetCustomerEmail(recipientEmail)
                .senderBankId(2)
                .receiverBankId(receiverBankId)
                .amount(amount)
                .currency(currency)
                .type(Transaction.TransactionType.INTERBANK_SEND)
                .status(Transaction.TransactionStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Created transaction: txnId={}, externalId={}", transaction.getId(), externalTxnId);

        try {
            CentralBankTransferRequest cbRequest = CentralBankTransferRequest.builder()
                    .externalTransactionId(externalTxnId)
                    .senderEmail(senderEmail)
                    .senderBankId(2)
                    .recipientEmail(recipientEmail)
                    .receiverBankId(receiverBankId)
                    .amount(amount)
                    .currency(currency)
                    .requestedAt(Instant.now())
                    .idempotencyKey(idempotencyKey)
                    .build();

            log.info("Sending transfer to Central Bank: {}", externalTxnId);
            CentralBankTransferResponse cbResponse = centralBankClient.sendTransferRequest(cbRequest);

            transaction.setCentralBankTransactionId(cbResponse.getCentralBankTransactionId());
            transaction.setSentToCentralBankAt(Instant.now());
            transaction.setStatus(Transaction.TransactionStatus.PROCESSING);
            transaction = transactionRepository.save(transaction);

            log.info("Transfer sent to Central Bank successfully: txnId={}, cbTxnId={}", 
                    externalTxnId, cbResponse.getCentralBankTransactionId());

            return transaction;

        } catch (CentralBankClient.CentralBankCommunicationException e) {
            log.error("Failed to send transfer to Central Bank: {}", externalTxnId, e);
            
            senderAccount.setBalance(senderAccount.getBalance().add(amount));
            accountRepository.save(senderAccount);
            log.info("Refunded sender account due to Central Bank communication failure");

            transaction.setStatus(Transaction.TransactionStatus.REJECTED);
            transaction.setRejectionReason("Failed to communicate with Central Bank: " + e.getMessage());
            transactionRepository.save(transaction);

            throw new TransactionException("Failed to initiate transfer: " + e.getMessage(), e);
        }
    }

    public Optional<Transaction> getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public Optional<Transaction> getTransactionByExternalId(String externalTransactionId) {
        return transactionRepository.findByExternalTransactionId(externalTransactionId);
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateIdempotencyKey() {
        return UUID.randomUUID().toString();
    }

    public static class TransactionException extends RuntimeException {
        public TransactionException(String message) {
            super(message);
        }

        public TransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
