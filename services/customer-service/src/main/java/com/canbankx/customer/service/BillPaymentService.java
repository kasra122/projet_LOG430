package com.canbankx.customer.service;

import com.canbankx.customer.domain.BillPayment;
import com.canbankx.customer.domain.Payee;
import com.canbankx.customer.domain.Account;
import com.canbankx.customer.dto.BillPaymentRequest;
import com.canbankx.customer.dto.BillPaymentResponse;
import com.canbankx.customer.repository.BillPaymentRepository;
import com.canbankx.customer.repository.PayeeRepository;
import com.canbankx.customer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillPaymentService {

    private final BillPaymentRepository billPaymentRepo;
    private final PayeeRepository payeeRepo;
    private final AccountRepository acctRepo;

    @Transactional
    public BillPaymentResponse processBillPayment(BillPaymentRequest req) {
        log.info("Processing bill payment for account: {}", req.getAccountId());

        Account acct = acctRepo.findById(req.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Payee payee = payeeRepo.findById(req.getPayeeId())
                .orElseThrow(() -> new RuntimeException("Payee not found"));

        if (acct.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds for bill payment");
        }

        BillPayment billPay = BillPayment.builder()
                .id(UUID.randomUUID())
                .accountId(req.getAccountId())
                .payeeId(req.getPayeeId())
                .amount(req.getAmount())
                .currency(acct.getCurrency())
                .status(BillPayment.PaymentStatus.PROCESSING)
                .refNum(generateRefNum())
                .paymentType(BillPayment.PaymentType.BILL)
                .notes(req.getNotes())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        if (req.getScheduledDate() != null && req.getScheduledDate().isAfter(Instant.now())) {
            billPay.setStatus(BillPayment.PaymentStatus.SCHEDULED);
            billPay.setScheduledDate(req.getScheduledDate());
        } else {
            executeBillPayment(billPay, acct);
        }

        BillPayment saved = billPaymentRepo.save(billPay);
        log.info("Bill payment processed: {}", saved.getRefNum());

        return mapToResponse(saved);
    }

    @Transactional
    private void executeBillPayment(BillPayment bp, Account acct) {
        try {
            acct.setBalance(acct.getBalance().subtract(bp.getAmount()));
            acctRepo.save(acct);

            bp.setStatus(BillPayment.PaymentStatus.COMPLETED);
            bp.setExecutedDate(Instant.now());

            log.info("Bill payment executed: {}", bp.getRefNum());
        } catch (Exception ex) {
            bp.setStatus(BillPayment.PaymentStatus.FAILED);
            log.error("Bill payment failed: {}", ex.getMessage());
            throw new RuntimeException("Failed to execute bill payment");
        }
    }

    public List<BillPaymentResponse> getPaymentsByAccount(UUID accountId) {
        return billPaymentRepo.findByAccountId(accountId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<Payee> getPayeesByCustomer(UUID customerId) {
        return payeeRepo.findByCustomerId(customerId);
    }

    @Transactional
    public Payee addPayee(UUID customerId, String name, String acctNum, String instNum, String transitNum) {
        Payee p = Payee.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .name(name)
                .accountNum(acctNum)
                .institutionNum(instNum)
                .transitNum(transitNum)
                .createdAt(Instant.now())
                .build();
        return payeeRepo.save(p);
    }

    private String generateRefNum() {
        return "BP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    private BillPaymentResponse mapToResponse(BillPayment bp) {
        return BillPaymentResponse.builder()
                .id(bp.getId())
                .accountId(bp.getAccountId())
                .payeeId(bp.getPayeeId())
                .amount(bp.getAmount())
                .status(bp.getStatus().toString())
                .refNum(bp.getRefNum())
                .createdAt(bp.getCreatedAt())
                .executedDate(bp.getExecutedDate())
                .build();
    }
}
