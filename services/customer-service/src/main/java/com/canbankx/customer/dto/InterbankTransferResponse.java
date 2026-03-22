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
public class InterbankTransferResponse {

    private UUID id;

    private String externalTransactionId;

    private String status;

    private BigDecimal amount;

    private Integer senderBankId;

    private Integer recipientBankId;

    private String recipientEmail;

    private Instant createdAt;

    private Instant settledAt;

    private String message;
}
