package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.SettlementNotificationRequest;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Processes settlement notification from Central Bank.
     * Updates transaction status and accounts accordingly.
     */
    @Transactional
    public void processSettlement(SettlementNotificationRequest notification) {
        log.info("Processing settlement notification: txnId={}, result={}", 
                notification.getExternalTransactionId(), notification.getResult());

        // Find our transaction
        Optional<Transaction> txnOpt = transactionRepository.findByExternalTransactionId(
                notification.getExternalTransactionId()
        );

        if (txnOpt.isEmpty()) {
            log.warn("Settlement notification received for unknown transaction: {}", 
                    notification.getExternalTransactionId());
            throw new SettlementException("Transaction not found: " + notification.getExternalTransactionId());
        }

        Transaction transaction = txnOpt.get();

        switch (notification.getResult()) {
            case "SETTLED" -> handleSettled(transaction, notification);
            case "REJECTED" -> handleRejected(transaction, notification);
            case "EXPIRED" -> handleExpired(transaction, notification);
            default -> {
                log.warn("Unknown settlement result: {}", notification.getResult());
                throw new SettlementException("Unknown settlement result: " + notification.getResult());
            }
        }

        transaction.setUpdatedAt(Instant.now());
        transactionRepository.save(transaction);
        
        log.info("Settlement processed successfully: txnId={}, newStatus={}", 
                transaction.getExternalTransactionId(), transaction.getStatus());
    }

    private void handleSettled(Transaction transaction, SettlementNotificationRequest notification) {
        log.info("Transfer SETTLED: {}", transaction.getId());
        
        transaction.setStatus(Transaction.TransactionStatus.SETTLED);
        transaction.setCentralBankTransactionId(notification.getCentralBankTransactionId());
        
        // For inter-bank transfers, the recipient's bank will credit their account
        // We just update our transaction status
        // (Recipient bank handles crediting their customer)
    }

    private void handleRejected(Transaction transaction, SettlementNotificationRequest notification) {
        log.warn("Transfer REJECTED by Central Bank: {}, reason: {}", 
                transaction.getId(), notification.getReason());
        
        transaction.setStatus(Transaction.TransactionStatus.REJECTED);
        transaction.setRejectionReason(notification.getReason());

        // Refund the sender if this was an outgoing transfer
        if (transaction.getType() == Transaction.TransactionType.INTERBANK_SEND) {
            refundSender(transaction);
        }
    }

    private void handleExpired(Transaction transaction, SettlementNotificationRequest notification) {
        log.warn("Transfer EXPIRED: {}, reason: {}", 
                transaction.getId(), notification.getReason());
        
        transaction.setStatus(Transaction.TransactionStatus.EXPIRED);
        transaction.setRejectionReason(notification.getReason());

        // Refund the sender if this was an outgoing transfer
        if (transaction.getType() == Transaction.TransactionType.INTERBANK_SEND) {
            refundSender(transaction);
        }
    }

    private void refundSender(Transaction transaction) {
        Optional<Account> senderAccountOpt = accountRepository.findById(transaction.getSourceAccountId());
        
        if (senderAccountOpt.isPresent()) {
            Account senderAccount = senderAccountOpt.get();
            senderAccount.setBalance(senderAccount.getBalance().add(transaction.getAmount()));
            accountRepository.save(senderAccount);
            
            log.info("Refunded sender account: accountId={}, amount={}", 
                    senderAccount.getId(), transaction.getAmount());
        } else {
            log.error("Could not refund - sender account not found: {}", transaction.getSourceAccountId());
        }
    }

    public static class SettlementException extends RuntimeException {
        public SettlementException(String message) {
            super(message);
        }
    }
}
