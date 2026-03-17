package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.InitiateTransferRequest;
import com.canbankx.customer.dto.TransactionResponse;
import com.canbankx.customer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Initiate an inter-bank transfer to another bank
     */
    @PostMapping("/initiate-transfer")
    public ResponseEntity<TransactionResponse> initiateTransfer(
            @RequestBody InitiateTransferRequest request) {
        
        try {
            log.info("Received transfer initiation request: from {} to {} at bank {}", 
                    request.getSenderEmail(), request.getRecipientEmail(), request.getReceiverBankId());

            Transaction transaction = transactionService.initiateInterbankTransfer(
                    request.getSenderAccountId(),
                    request.getSenderEmail(),
                    request.getRecipientEmail(),
                    request.getReceiverBankId(),
                    request.getAmount(),
                    request.getCurrency()
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(mapToResponse(transaction));

        } catch (TransactionService.TransactionException e) {
            log.error("Transfer initiation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get transaction status by ID
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID transactionId) {
        Optional<Transaction> txn = transactionService.getTransaction(transactionId);
        
        if (txn.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(txn.get()));
    }

    /**
     * Get transaction status by external ID
     */
    @GetMapping("/by-external-id/{externalTransactionId}")
    public ResponseEntity<TransactionResponse> getTransactionByExternalId(
            @PathVariable String externalTransactionId) {
        
        Optional<Transaction> txn = transactionService.getTransactionByExternalId(externalTransactionId);
        
        if (txn.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(txn.get()));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .externalTransactionId(transaction.getExternalTransactionId())
                .centralBankTransactionId(transaction.getCentralBankTransactionId())
                .senderEmail(transaction.getSourceCustomerEmail())
                .recipientEmail(transaction.getTargetCustomerEmail())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType().toString())
                .status(transaction.getStatus().toString())
                .rejectionReason(transaction.getRejectionReason())
                .createdAt(transaction.getCreatedAt())
                .sentToCentralBankAt(transaction.getSentToCentralBankAt())
                .settledAt(transaction.getSettledAt())
                .build();
    }
}
