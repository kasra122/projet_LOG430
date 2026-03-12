package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public Account createAccount(UUID customerId, String currency) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        Account account = Account.builder()
                .customerId(customerId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        log.info("Creating account for customer {} with currency {}", customerId, currency);
        return accountRepository.save(account);
    }

    public List<Account> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}