package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transactions", description = "Deposits, withdrawals, and transfers (UC-05, UC-06)")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into an account")
    public Transaction deposit(
            @RequestParam @NotNull UUID accountId,
            @RequestParam @NotNull @Positive(message = "Amount must be positive") BigDecimal amount) {

        return transactionService.deposit(accountId, amount);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from an account")
    public Transaction withdraw(
            @RequestParam @NotNull UUID accountId,
            @RequestParam @NotNull @Positive(message = "Amount must be positive") BigDecimal amount) {

        return transactionService.withdraw(accountId, amount);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts (UC-05)")
    public Transaction transfer(
            @RequestParam @NotNull UUID sourceAccountId,
            @RequestParam @NotNull UUID targetAccountId,
            @RequestParam @NotNull @Positive(message = "Amount must be positive") BigDecimal amount) {

        return transactionService.transfer(sourceAccountId, targetAccountId, amount);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get transaction history for an account (UC-04)")
    public List<Transaction> getTransactions(@PathVariable UUID accountId) {

        return transactionService.getAccountTransactions(accountId);
    }
}