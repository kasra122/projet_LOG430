package com.canbankx.customer.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementNotificationRequest {

    @NotBlank(message = "Transaction ID required")
    private String externalTransactionId;

    @NotBlank(message = "Settlement result required")
    private String result;

    private String reason;
}
