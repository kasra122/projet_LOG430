package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Payee;
import com.canbankx.customer.dto.BillPaymentRequest;
import com.canbankx.customer.dto.BillPaymentResponse;
import com.canbankx.customer.service.BillPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/bill-payments")
@RequiredArgsConstructor
public class BillPaymentController {

    private final BillPaymentService billPaySvc;

    @PostMapping("/pay")
    public ResponseEntity<BillPaymentResponse> processBillPayment(
            @Valid @RequestBody BillPaymentRequest req) {
        log.info("Received bill payment request for account: {}", req.getAccountId());
        BillPaymentResponse res = billPaySvc.processBillPayment(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<BillPaymentResponse>> getPayments(
            @PathVariable UUID accountId) {
        List<BillPaymentResponse> payments = billPaySvc.getPaymentsByAccount(accountId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payees/{customerId}")
    public ResponseEntity<List<Payee>> getPayees(
            @PathVariable UUID customerId) {
        List<Payee> payees = billPaySvc.getPayeesByCustomer(customerId);
        return ResponseEntity.ok(payees);
    }

    @PostMapping("/payees/{customerId}")
    public ResponseEntity<Payee> addPayee(
            @PathVariable UUID customerId,
            @RequestParam String name,
            @RequestParam String accountNum,
            @RequestParam String institutionNum,
            @RequestParam String transitNum) {
        Payee p = billPaySvc.addPayee(customerId, name, accountNum, institutionNum, transitNum);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }
}
