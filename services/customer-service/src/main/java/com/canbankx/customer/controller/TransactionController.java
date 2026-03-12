package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.service.TransactionService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public Transaction deposit(
            @RequestParam @NotNull UUID accountId,
            @RequestParam @NotNull @Positive(message = "Amount must be positive") BigDecimal amount) {

        return transactionService.deposit(accountId, amount);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(
            @RequestParam @NotNull UUID accountId,
            @RequestParam @NotNull @Positive(message = "Amount must be positive") BigDecimal amount) {

        return transactionService.withdraw(accountId, amount);
    }

    @PostMapping("/transfer")
    public Transaction transfer(
            @RequestParam @NotNull UUID sourceAccountId,
            @RequestParam @NotNull UUID targetAccountId,
            @RequestParam @NotNull @Positive(message = "Amount must be positive") BigDecimal amount) {

        return transactionService.transfer(sourceAccountId, targetAccountId, amount);
    }

    @GetMapping("/{accountId}")
    public List<Transaction> getTransactions(@PathVariable UUID accountId) {

        return transactionService.getAccountTransactions(accountId);
    }
}