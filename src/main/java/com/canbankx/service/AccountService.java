package com.canbankx.service;

import com.canbankx.domain.Account;
import com.canbankx.exception.InvalidRequestException;
import com.canbankx.exception.ResourceNotFoundException;
import com.canbankx.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(UUID customerId, String currency) {
        if (customerId == null) {
            throw new InvalidRequestException("customerId", "Customer ID cannot be null");
        }

        if (currency == null || currency.isBlank()) {
            throw new InvalidRequestException("currency", "Currency cannot be empty");
        }

        if (!isValidCurrency(currency)) {
            throw new InvalidRequestException("currency", "Invalid currency code: " + currency);
        }

        Account account = Account.builder()
                .customerId(customerId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByCustomer(UUID customerId) {
        if (customerId == null) {
            throw new InvalidRequestException("customerId", "Customer ID cannot be null");
        }

        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("Account", "customerId", customerId);
        }

        return accounts;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    private boolean isValidCurrency(String currency) {
        // Simple validation for common currency codes
        return currency.matches("[A-Z]{3}");
    }
}