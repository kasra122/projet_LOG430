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
public class BillPaymentResponse {

    private UUID id;
    private UUID accountId;
    private UUID payeeId;
    private BigDecimal amount;
    private String status;
    private String refNum;
    private Instant createdAt;
    private Instant executedDate;
}
