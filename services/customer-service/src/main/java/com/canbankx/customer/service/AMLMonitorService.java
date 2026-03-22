package com.canbankx.customer.service;

import com.canbankx.customer.domain.*;
import com.canbankx.customer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AMLMonitorService {

    private final TransactionRepository txnRepo;
    private final SuspiciousActivityReportRepository sarRepo;
    private final AMLRuleRepository amlRuleRepo;

    @Transactional
    public void analyzeTransaction(Transaction txn) {
        log.info("Running AML analysis on transaction: {}", txn.getId());

        List<AMLRule> activeRules = amlRuleRepo.findByActive(true);

        for (AMLRule rule : activeRules) {
            boolean triggered = evaluateRule(txn, rule);
            if (triggered) {
                createSuspiciousActivityReport(txn, rule);
            }
        }
    }

    private boolean evaluateRule(Transaction txn, AMLRule rule) {
        switch (rule.getRuleType()) {
            case AMOUNT_THRESHOLD:
                return checkAmountThreshold(txn, rule);
            case VELOCITY_CHECK:
                return checkVelocity(txn, rule);
            case FREQUENCY_LIMIT:
                return checkFrequency(txn, rule);
            case PATTERN_DETECTION:
                return detectPattern(txn, rule);
            default:
                return false;
        }
    }

    private boolean checkAmountThreshold(Transaction txn, AMLRule rule) {
        if (txn.getAmount().compareTo(rule.getThreshold()) > 0) {
            log.warn("Amount threshold triggered for transaction: {} amount: {}", 
                    txn.getId(), txn.getAmount());
            return true;
        }
        return false;
    }

    private boolean checkVelocity(Transaction txn, AMLRule rule) {
        Instant timeWindow = Instant.now().minusSeconds(rule.getTimeWindowMinutes() * 60);
        long recentTxns = txnRepo.findBySourceAccountIdAndCreatedAtAfter(
                txn.getSourceAccountId(), timeWindow).size();

        if (recentTxns > 5) {
            log.warn("High velocity detected for account: {} txns: {}", 
                    txn.getSourceAccountId(), recentTxns);
            return true;
        }
        return false;
    }

    private boolean checkFrequency(Transaction txn, AMLRule rule) {
        Instant timeWindow = Instant.now().minusSeconds(rule.getTimeWindowMinutes() * 60);
        long dailyCount = txnRepo.findBySourceAccountIdAndCreatedAtAfter(
                txn.getSourceAccountId(), timeWindow).size();

        int maxTransactionsPerDay = 10;
        if (dailyCount > maxTransactionsPerDay) {
            log.warn("Frequency limit exceeded for account: {} count: {}", 
                    txn.getSourceAccountId(), dailyCount);
            return true;
        }
        return false;
    }

    private boolean detectPattern(Transaction txn, AMLRule rule) {
        if (txn.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            log.warn("High-value pattern detected: {}", txn.getId());
            return true;
        }
        return false;
    }

    @Transactional
    private void createSuspiciousActivityReport(Transaction txn, AMLRule rule) {
        SuspiciousActivityReport sar = SuspiciousActivityReport.builder()
                .id(UUID.randomUUID())
                .customerId(txn.getSourceAccountId())
                .transactionId(txn.getId())
                .status(SuspiciousActivityReport.ReportStatus.PENDING_REVIEW)
                .ruleTriggered(rule.getRuleName())
                .description("Rule: " + rule.getRuleName() + ", Amount: " + txn.getAmount())
                .severity(rule.getSeverity())
                .reportedToFintrac(false)
                .createdAt(Instant.now())
                .build();

        sarRepo.save(sar);
        log.info("Suspicious activity report created: {} for rule: {}", sar.getId(), rule.getRuleName());
    }

    public List<SuspiciousActivityReport> getPendingReports() {
        return sarRepo.findByStatus(SuspiciousActivityReport.ReportStatus.PENDING_REVIEW);
    }

    @Transactional
    public void reportToFintrac(UUID sarId) {
        SuspiciousActivityReport sar = sarRepo.findById(sarId)
                .orElseThrow(() -> new RuntimeException("SAR not found"));

        sar.setReportedToFintrac(true);
        sar.setStatus(SuspiciousActivityReport.ReportStatus.REPORTED);
        sarRepo.save(sar);

        log.info("SAR reported to FINTRAC: {}", sarId);
    }
}
