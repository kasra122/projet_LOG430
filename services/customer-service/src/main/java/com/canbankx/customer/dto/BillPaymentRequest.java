package com.canbankx.customer.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPaymentRequest {

    @NotNull(message = "Account ID required")
    private UUID accountId;

    @NotNull(message = "Payee ID required")
    private UUID payeeId;

    @NotNull(message = "Amount required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private Instant scheduledDate;

    private String notes;
}
