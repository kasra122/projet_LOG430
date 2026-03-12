package com.canbankx.service;

import com.canbankx.domain.Account;
import com.canbankx.domain.Transaction;
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

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

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

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
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

        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account target = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new RuntimeException("Target account not found"));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
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

        return transactionRepository
                .findBySourceAccountIdOrTargetAccountId(accountId, accountId);
    }
}