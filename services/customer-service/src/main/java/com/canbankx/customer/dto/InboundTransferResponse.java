package com.canbankx.customer.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundTransferResponse {

    private String externalTransactionId;
    private String status;
    private String reason;
    private Instant processedAt;
}
