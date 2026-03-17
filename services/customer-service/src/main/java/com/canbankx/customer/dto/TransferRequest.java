package com.canbankx.customer.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @NotBlank(message = "Source account ID required")
    private String sourceAccountId;

    @NotBlank(message = "Target email required")
    @Email(message = "Valid email required")
    private String targetCustomerEmail;

    @NotNull(message = "Amount required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Receiver bank ID required")
    private Integer receiverBankId;

    private String idempotencyKey;
}
