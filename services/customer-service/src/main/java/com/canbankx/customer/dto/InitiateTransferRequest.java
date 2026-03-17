package com.canbankx.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateTransferRequest {
    
    private UUID senderAccountId;
    private String senderEmail;
    private String recipientEmail;
    private Integer receiverBankId;
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "CAD";
}
