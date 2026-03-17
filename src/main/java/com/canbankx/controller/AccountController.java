package com.canbankx.controller;

import com.canbankx.domain.Account;
import com.canbankx.exception.InvalidRequestException;
import com.canbankx.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestParam UUID customerId,
            @RequestParam String currency
    ) {
        if (customerId == null) {
            throw new InvalidRequestException("customerId", "Customer ID cannot be null");
        }

        if (currency == null || currency.isBlank()) {
            throw new InvalidRequestException("currency", "Currency cannot be empty");
        }

        Account account = accountService.createAccount(customerId, currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<Account>> getAccountsByCustomer(@PathVariable UUID customerId) {
        if (customerId == null) {
            throw new InvalidRequestException("customerId", "Customer ID cannot be null");
        }

        List<Account> accounts = accountService.getAccountsByCustomer(customerId);
        return ResponseEntity.ok(accounts);
    }
}