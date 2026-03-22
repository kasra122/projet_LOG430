package com.canbankx.customer.service;

import com.canbankx.customer.domain.*;
import com.canbankx.customer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EODService {

    private final AccountRepository acctRepo;
    private final TransactionRepository txnRepo;
    private final StatementRepository stmtRepo;
    private final AuditLogRepository auditRepo;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runEndOfDay() {
        log.info("Starting EOD process");
        Instant startTime = Instant.now();

        try {
            List<Account> allAccounts = acctRepo.findAll();
            
            for (Account acct : allAccounts) {
                processAccountEOD(acct);
            }

            log.info("EOD process completed in {} ms", 
                    Instant.now().toEpochMilli() - startTime.toEpochMilli());

        } catch (Exception ex) {
            log.error("EOD process failed: {}", ex.getMessage());
        }
    }

    @Transactional
    private void processAccountEOD(Account acct) {
        log.info("Processing EOD for account: {}", acct.getId());

        LocalDate yesterday = LocalDate.now(ZoneId.of("America/Toronto")).minusDays(1);
        
        Instant startOfDay = yesterday.atStartOfDay(ZoneId.of("America/Toronto")).toInstant();
        Instant endOfDay = yesterday.atTime(23, 59, 59).atZone(ZoneId.of("America/Toronto")).toInstant();

        List<Transaction> dailyTxns = txnRepo.findBySourceAccountIdAndCreatedAtBetween(
                acct.getId(), startOfDay, endOfDay);

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Transaction txn : dailyTxns) {
            if (txn.getType() == Transaction.TransactionType.WITHDRAW || 
                txn.getType() == Transaction.TransactionType.TRANSFER) {
                totalDebits = totalDebits.add(txn.getAmount());
            } else {
                totalCredits = totalCredits.add(txn.getAmount());
            }
        }

        BigDecimal interestAccrued = calculateInterest(acct);
        acct.setBalance(acct.getBalance().add(interestAccrued));
        acctRepo.save(acct);

        Statement stmt = Statement.builder()
                .id(java.util.UUID.randomUUID())
                .accountId(acct.getId())
                .statementDate(yesterday)
                .openingBalance(acct.getBalance().subtract(totalCredits).add(totalDebits))
                .closingBalance(acct.getBalance())
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .accruedInterest(interestAccrued)
                .txnCount(dailyTxns.size())
                .createdAt(Instant.now())
                .build();

        stmtRepo.save(stmt);

        logAuditEntry("STATEMENT_GENERATED", acct.getId().toString(), stmt.getId().toString());
        log.info("Statement generated for account: {} on {}", acct.getId(), yesterday);
    }

    private BigDecimal calculateInterest(Account acct) {
        if (acct.getAccountType() == Account.AccountType.SAVINGS) {
            BigDecimal annualRate = new BigDecimal("0.02");
            return acct.getBalance().multiply(annualRate).divide(new BigDecimal("365"), 2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private void logAuditEntry(String action, String entityId, String details) {
        AuditLog log = AuditLog.builder()
                .id(java.util.UUID.randomUUID())
                .entityType("ACCOUNT")
                .entityId(entityId)
                .action(action)
                .newValue(details)
                .createdAt(Instant.now())
                .build();
        auditRepo.save(log);
    }

    public List<Statement> getAccountStatements(java.util.UUID accountId) {
        return stmtRepo.findByAccountId(accountId);
    }
}
