package com.canbankx.customer.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundTransferRequest {

    @NotBlank(message = "External transaction ID required")
    private String externalTransactionId;

    @NotBlank(message = "Sender email required")
    @Email(message = "Valid email required")
    private String senderCustomerEmail;

    @NotBlank(message = "Recipient email required")
    @Email(message = "Valid email required")
    private String recipientEmail;

    @NotNull(message = "Amount required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Sender bank ID required")
    private Integer senderBankId;

    private String currency = "CAD";
}
