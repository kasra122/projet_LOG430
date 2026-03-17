package com.canbankx.customer.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private UUID transactionId;
    private String status;
    private BigDecimal amount;
    private String targetEmail;
    private Integer receiverBankId;
    private Instant createdAt;
    private String message;
}
