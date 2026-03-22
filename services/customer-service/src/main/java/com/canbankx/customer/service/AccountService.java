package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository acctRepo;

    @Transactional
    public Account createAccount(UUID customerId, String currency) {
        log.info("Creating account for customer: {}", customerId);
        Account acct = Account.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .accountNum(generateAccountNumber())
                .accountType(Account.AccountType.CHECKING)
                .currency(currency != null ? currency : "CAD")
                .balance(BigDecimal.ZERO)
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return acctRepo.save(acct);
    }

    public List<Account> getAllAccounts() {
        return acctRepo.findAll();
    }

    public List<Account> getAccountsByCustomer(UUID customerId) {
        return acctRepo.findByCustomerId(customerId);
    }

    @Transactional
    public void deductBalance(Account acct, BigDecimal amount) {
        log.info("Deducting {} from account {}", amount, acct.getId());
        acct.setBalance(acct.getBalance().subtract(amount));
        acctRepo.save(acct);
    }

    @Transactional
    public void refundBalance(Account acct, BigDecimal amount) {
        log.info("Refunding {} to account {}", amount, acct.getId());
        acct.setBalance(acct.getBalance().add(amount));
        acctRepo.save(acct);
    }

    @Transactional
    public void creditBalance(Account acct, BigDecimal amount) {
        log.info("Crediting {} to account {}", amount, acct.getId());
        acct.setBalance(acct.getBalance().add(amount));
        acctRepo.save(acct);
    }

    private String generateAccountNumber() {
        return "CA" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
    }
}
