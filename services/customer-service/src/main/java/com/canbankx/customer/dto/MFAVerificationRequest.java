package com.canbankx.customer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MFAVerificationRequest {

    @NotBlank(message = "OTP Token required")
    private String otpToken;

    @NotBlank(message = "OTP Code required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otpCode;

    private Boolean trustDevice = false;
}
