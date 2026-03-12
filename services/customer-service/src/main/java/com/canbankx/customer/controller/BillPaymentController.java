package com.canbankx.customer.controller;

import com.canbankx.customer.domain.BillPayment;
import com.canbankx.customer.service.BillPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Validated
@Tag(name = "Bill Payments", description = "Bill payment processing (UC-06)")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    @PostMapping("/pay")
    @Operation(summary = "Pay a bill from an account")
    public BillPayment payBill(
            @RequestParam @NotNull UUID accountId,
            @RequestParam @NotNull String payee,
            @RequestParam @NotNull @Positive(message = "Amount must be positive") BigDecimal amount,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        return billPaymentService.payBill(accountId, payee, amount, idempotencyKey);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get bill payment history for an account")
    public List<BillPayment> getPayments(@PathVariable UUID accountId) {
        return billPaymentService.getPaymentsByAccount(accountId);
    }
}
