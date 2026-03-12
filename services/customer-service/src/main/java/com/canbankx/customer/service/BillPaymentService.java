package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.BillPayment;
import com.canbankx.customer.exception.InsufficientFundsException;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.BillPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillPaymentService {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentService.class);

    private final BillPaymentRepository billPaymentRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    @Transactional
    public BillPayment payBill(UUID accountId, String payee, BigDecimal amount, String idempotencyKey) {

        // Idempotency check
        if (idempotencyKey != null) {
            Optional<BillPayment> existing = billPaymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent bill payment returned for key: {}", idempotencyKey);
                return existing.get();
            }
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account: " + accountId);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        BillPayment payment = BillPayment.builder()
                .accountId(accountId)
                .payee(payee)
                .amount(amount)
                .status("COMPLETED")
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .build();

        BillPayment saved = billPaymentRepository.save(payment);

        auditService.logAction("BILL_PAYMENT", saved.getId().toString(), "PAY_BILL",
                "Bill payment of " + amount + " to " + payee + " from account " + accountId, "SYSTEM");

        log.info("Bill payment of {} to {} from account {}", amount, payee, accountId);
        return saved;
    }

    public List<BillPayment> getPaymentsByAccount(UUID accountId) {
        return billPaymentRepository.findByAccountId(accountId);
    }
}
