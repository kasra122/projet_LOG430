package com.canbankx.customer.service;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AmlService {

    private static final Logger log = LoggerFactory.getLogger(AmlService.class);

    private static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("10000");
    private static final int HIGH_FREQUENCY_THRESHOLD = 10;

    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    /**
     * Checks a transaction for AML suspicious activity patterns:
     * 1. Large transaction amounts (> $10,000)
     * 2. High-frequency transactions (> 10 in 24h for same account)
     *
     * Returns true if the transaction is flagged as suspicious.
     */
    public boolean checkTransaction(Transaction transaction) {
        boolean flagged = false;

        // Rule 1: Large transaction detection
        if (transaction.getAmount().compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
            flagged = true;
            log.warn("AML ALERT: Large transaction detected - amount={}, account={}, type={}",
                    transaction.getAmount(), transaction.getSourceAccountId(), transaction.getType());

            auditService.logAction("AML", transaction.getId().toString(), "LARGE_TRANSACTION",
                    "Transaction amount " + transaction.getAmount() + " exceeds threshold of " + LARGE_TRANSACTION_THRESHOLD,
                    "AML_SYSTEM");
        }

        // Rule 2: High frequency detection (queried at DB level for performance)
        UUID accountId = transaction.getSourceAccountId();
        Instant oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        long recentCount = transactionRepository.countBySourceAccountIdAndCreatedAtAfter(accountId, oneDayAgo);

        if (recentCount > HIGH_FREQUENCY_THRESHOLD) {
            flagged = true;
            log.warn("AML ALERT: High frequency transactions detected - count={} in 24h, account={}",
                    recentCount, accountId);

            auditService.logAction("AML", transaction.getId().toString(), "HIGH_FREQUENCY",
                    "Account " + accountId + " has " + recentCount + " transactions in 24h (threshold: " + HIGH_FREQUENCY_THRESHOLD + ")",
                    "AML_SYSTEM");
        }

        return flagged;
    }
}
