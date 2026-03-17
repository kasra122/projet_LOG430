package com.canbankx.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private UUID id;
    private String externalTransactionId;
    private String centralBankTransactionId;
    private String senderEmail;
    private String recipientEmail;
    private BigDecimal amount;
    private String currency;
    private String type;
    private String status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant sentToCentralBankAt;
    private Instant settledAt;
}
