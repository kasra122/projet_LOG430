package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.InboundTransferRequest;
import com.canbankx.customer.dto.InboundTransferResponse;
import com.canbankx.customer.dto.SettlementNotificationRequest;
import com.canbankx.customer.dto.TransferRequest;
import com.canbankx.customer.dto.TransferResponse;
import com.canbankx.customer.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount) {

        return ResponseEntity.ok(transactionService.deposit(accountId, amount));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount) {

        return ResponseEntity.ok(transactionService.withdraw(accountId, amount));
    }

    @PostMapping("/transfer-local")
    public ResponseEntity<Transaction> transferLocal(
            @RequestParam UUID sourceAccountId,
            @RequestParam UUID targetAccountId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String idempotencyKey) {

        Transaction transaction = transactionService.transferLocal(sourceAccountId, targetAccountId, amount, idempotencyKey);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/transfer-interbank")
    public ResponseEntity<TransferResponse> transferInterbank(
            @Valid @RequestBody TransferRequest request) {

        log.info("Interbank transfer request: {} -> {}", request.getSourceAccountId(), request.getTargetCustomerEmail());

        try {
            UUID sourceAccountId = UUID.fromString(request.getSourceAccountId());
            Transaction transaction = transactionService.transferInterbank(
                    sourceAccountId,
                    request.getTargetCustomerEmail(),
                    request.getAmount(),
                    request.getReceiverBankId(),
                    request.getIdempotencyKey()
            );

            TransferResponse response = TransferResponse.builder()
                    .transactionId(transaction.getId())
                    .status(transaction.getStatus().toString())
                    .amount(transaction.getAmount())
                    .targetEmail(transaction.getTargetCustomerEmail())
                    .receiverBankId(transaction.getReceiverBankId())
                    .createdAt(transaction.getCreatedAt())
                    .message("Transfer initiated")
                    .build();

            return ResponseEntity.accepted().body(response);

        } catch (TransactionService.InsufficientFundsException e) {
            log.error("Insufficient funds: {}", e.getMessage());
            TransferResponse response = TransferResponse.builder()
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (TransactionService.TransactionException e) {
            log.error("Transaction error: {}", e.getMessage());
            TransferResponse response = TransferResponse.builder()
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/inbound-transfer")
    public ResponseEntity<InboundTransferResponse> receiveInterbankTransfer(
            @Valid @RequestBody InboundTransferRequest request) {

        log.info("Receiving inbound transfer: {} from Bank {}", request.getRecipientEmail(), request.getSenderBankId());

        InboundTransferResponse response = transactionService.receiveInterbankTransfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/settlement-notification")
    public ResponseEntity<Void> processSettlementNotification(
            @Valid @RequestBody SettlementNotificationRequest request) {

        log.info("Settlement notification: {} -> {}", request.getExternalTransactionId(), request.getResult());

        transactionService.processSettlementNotification(
                request.getExternalTransactionId(),
                request.getResult(),
                request.getReason()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable UUID accountId) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Optional<Transaction>> getTransaction(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }
}