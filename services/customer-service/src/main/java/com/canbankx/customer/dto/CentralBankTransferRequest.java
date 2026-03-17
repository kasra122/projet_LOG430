package com.canbankx.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentralBankTransferRequest {
    
    // Our internal transaction ID
    private String externalTransactionId;
    
    // Sender details
    private String senderEmail;
    private Integer senderBankId;
    
    // Recipient details
    private String recipientEmail;
    private Integer receiverBankId;
    
    // Transfer details
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "CAD";
    
    // Timestamp for ordering
    private Instant requestedAt;
    
    // Idempotency
    private String idempotencyKey;
}
