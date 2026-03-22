package com.canbankx.customer.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MFAVerificationResponse {

    private String msg;

    private String sessionToken;

    private Long expiresIn;

    private Boolean mfaVerified;
}
