package com.canbankx.customer.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRegisterResponse {

    private UUID customerId;
    private String email;
    private String firstName;
    private String lastName;
    private String kycStatus;
    private String message;
}