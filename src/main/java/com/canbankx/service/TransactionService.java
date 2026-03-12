package com.canbankx.service;

import com.canbankx.domain.Account;
import com.canbankx.domain.Transaction;
import com.canbankx.exception.InsufficientFundsException;
import com.canbankx.exception.InvalidRequestException;
import com.canbankx.exception.ResourceNotFoundException;
import com.canbankx.repository.AccountRepository;
import com.canbankx.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount) {
        if (accountId == null) {
            throw new InvalidRequestException("accountId", "Account ID cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("amount", "Deposit amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .targetAccountId(accountId)
                .amount(amount)
                .type("DEPOSIT")
                .createdAt(Instant.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(UUID accountId, BigDecimal amount) {
        if (accountId == null) {
            throw new InvalidRequestException("accountId", "Account ID cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("amount", "Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Account balance: " + account.getBalance() + ", requested: " + amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(accountId)
                .amount(amount)
                .type("WITHDRAW")
                .createdAt(Instant.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
        if (sourceAccountId == null) {
            throw new InvalidRequestException("sourceAccountId", "Source account ID cannot be null");
        }

        if (targetAccountId == null) {
            throw new InvalidRequestException("targetAccountId", "Target account ID cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("amount", "Transfer amount must be greater than zero");
        }

        if (sourceAccountId.equals(targetAccountId)) {
            throw new InvalidRequestException("targetAccountId", "Source and target accounts cannot be the same");
        }

        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", sourceAccountId));

        Account target = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", targetAccountId));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Source account balance: " + source.getBalance() + ", requested: " + amount);
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

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(UUID accountId) {
        if (accountId == null) {
            throw new InvalidRequestException("accountId", "Account ID cannot be null");
        }

        return transactionRepository
                .findBySourceAccountIdOrTargetAccountId(accountId, accountId);
    }
}