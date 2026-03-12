package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public Account createAccount(
            @RequestParam UUID customerId,
            @RequestParam String currency
    ) {
        return accountService.createAccount(customerId, currency);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{customerId}")
    public List<Account> getAccountsByCustomer(@PathVariable UUID customerId) {
        return accountService.getAccountsByCustomer(customerId);
    }
}