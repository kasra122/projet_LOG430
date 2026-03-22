package com.canbankx.customer.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String msg;

    private String sessionToken;

    private Boolean mfaRequired;

    private String otpToken;

    private String deliveryMethod;
}
