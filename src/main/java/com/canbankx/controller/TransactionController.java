package com.canbankx.controller;

import com.canbankx.domain.Transaction;
import com.canbankx.exception.InvalidRequestException;
import com.canbankx.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount) {

        if (accountId == null) {
            throw new InvalidRequestException("accountId", "Account ID cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("amount", "Amount must be greater than zero");
        }

        Transaction transaction = transactionService.deposit(accountId, amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount) {

        if (accountId == null) {
            throw new InvalidRequestException("accountId", "Account ID cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("amount", "Amount must be greater than zero");
        }

        Transaction transaction = transactionService.withdraw(accountId, amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            @RequestParam UUID sourceAccountId,
            @RequestParam UUID targetAccountId,
            @RequestParam BigDecimal amount) {

        if (sourceAccountId == null) {
            throw new InvalidRequestException("sourceAccountId", "Source account ID cannot be null");
        }

        if (targetAccountId == null) {
            throw new InvalidRequestException("targetAccountId", "Target account ID cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("amount", "Amount must be greater than zero");
        }

        if (sourceAccountId.equals(targetAccountId)) {
            throw new InvalidRequestException("targetAccountId", "Source and target accounts cannot be the same");
        }

        Transaction transaction = transactionService.transfer(sourceAccountId, targetAccountId, amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable UUID accountId) {
        if (accountId == null) {
            throw new InvalidRequestException("accountId", "Account ID cannot be null");
        }

        List<Transaction> transactions = transactionService.getAccountTransactions(accountId);
        return ResponseEntity.ok(transactions);
    }
}