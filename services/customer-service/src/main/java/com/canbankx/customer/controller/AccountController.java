package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management (UC-03)")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Open a new bank account (CHECKING or SAVINGS)")
    public Account createAccount(
            @RequestParam UUID customerId,
            @RequestParam(defaultValue = "CHECKING") String accountType,
            @RequestParam(defaultValue = "CAD") String currency
    ) {
        return accountService.createAccount(customerId, accountType, currency);
    }

    @GetMapping
    @Operation(summary = "List all accounts")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get accounts by customer ID (UC-04)")
    public List<Account> getAccountsByCustomer(@PathVariable UUID customerId) {
        return accountService.getAccountsByCustomer(customerId);
    }
}