package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public Transaction deposit(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount) {

        return transactionService.deposit(accountId, amount);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount) {

        return transactionService.withdraw(accountId, amount);
    }

    @PostMapping("/transfer")
    public Transaction transfer(
            @RequestParam UUID sourceAccountId,
            @RequestParam UUID targetAccountId,
            @RequestParam BigDecimal amount) {

        return transactionService.transfer(sourceAccountId, targetAccountId, amount);
    }

    @GetMapping("/{accountId}")
    public List<Transaction> getTransactions(@PathVariable UUID accountId) {

        return transactionService.getAccountTransactions(accountId);
    }
}