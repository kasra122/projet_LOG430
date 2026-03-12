package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.exception.InsufficientFundsException;
import com.canbankx.customer.exception.InvalidAmountException;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount) {

        validateAmount(amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(accountId)
                .amount(amount)
                .type("DEPOSIT")
                .createdAt(Instant.now())
                .build();

        log.info("Deposit of {} to account {}", amount, accountId);
        Transaction saved = transactionRepository.save(transaction);

        auditService.logAction("TRANSACTION", saved.getId().toString(), "DEPOSIT",
                "Deposit of " + amount + " to account " + accountId, "SYSTEM");

        return saved;
    }

    @Transactional
    public Transaction withdraw(UUID accountId, BigDecimal amount) {

        validateAmount(amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account: " + accountId);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(accountId)
                .amount(amount)
                .type("WITHDRAW")
                .createdAt(Instant.now())
                .build();

        log.info("Withdrawal of {} from account {}", amount, accountId);
        Transaction saved = transactionRepository.save(transaction);

        auditService.logAction("TRANSACTION", saved.getId().toString(), "WITHDRAW",
                "Withdrawal of " + amount + " from account " + accountId, "SYSTEM");

        return saved;
    }

    @Transactional
    public Transaction transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {

        validateAmount(amount);

        if (sourceAccountId.equals(targetAccountId)) {
            throw new InvalidAmountException("Source and target accounts must be different");
        }

        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + sourceAccountId));

        Account target = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found: " + targetAccountId));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account: " + sourceAccountId);
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(target);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .type("TRANSFER")
                .createdAt(Instant.now())
                .build();

        log.info("Transfer of {} from account {} to account {}", amount, sourceAccountId, targetAccountId);
        Transaction saved = transactionRepository.save(transaction);

        auditService.logAction("TRANSACTION", saved.getId().toString(), "TRANSFER",
                "Transfer of " + amount + " from " + sourceAccountId + " to " + targetAccountId, "SYSTEM");

        return saved;
    }

    public List<Transaction> getAccountTransactions(UUID accountId) {

        return transactionRepository
                .findBySourceAccountIdOrTargetAccountId(accountId, accountId);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }
}