package com.canbankx.service;

import com.canbankx.domain.Account;
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

        Account account = Account.builder()
                .customerId(customerId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}