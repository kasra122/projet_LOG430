package com.canbankx.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentralBankTransferResponse {
    
    // Our internal transaction ID (echo back)
    private String externalTransactionId;
    
    // Central Bank's transaction ID
    private String centralBankTransactionId;
    
    // Status: ACCEPTED, REJECTED, PENDING
    private String status;
    
    // Reason if rejected
    private String reason;
    
    // When Central Bank processed it
    private Instant processedAt;
}
