package com.canbankx.customer.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterbankTransferRequest {

    @NotNull(message = "Source account ID required")
    private UUID sourceAccountId;

    @NotBlank(message = "Recipient email required")
    @Email(message = "Invalid recipient email")
    private String recipientEmail;

    @NotNull(message = "Amount required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Recipient bank ID required")
    private Integer recipientBankId;

    private String currency;

    @NotBlank(message = "Idempotency key required")
    private String idempotencyKey;
}
