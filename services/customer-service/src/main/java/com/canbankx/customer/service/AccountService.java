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
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private static final Set<String> VALID_ACCOUNT_TYPES = Set.of("CHECKING", "SAVINGS");

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public Account createAccount(UUID customerId, String accountType, String currency) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        if (!VALID_ACCOUNT_TYPES.contains(accountType)) {
            throw new IllegalArgumentException("Invalid account type: " + accountType + ". Must be CHECKING or SAVINGS");
        }

        Account account = Account.builder()
                .customerId(customerId)
                .accountType(accountType)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        Account saved = accountRepository.save(account);

        auditService.logAction("ACCOUNT", saved.getId().toString(), "CREATE",
                "Account created: type=" + accountType + ", currency=" + currency, customerId.toString());

        log.info("Created {} account for customer {} with currency {}", accountType, customerId, currency);
        return saved;
    }

    public List<Account> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}