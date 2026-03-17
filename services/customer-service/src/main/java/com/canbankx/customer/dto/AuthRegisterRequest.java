package com.canbankx.customer.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRegisterRequest {

    @NotBlank(message = "First name required")
    private String firstName;

    @NotBlank(message = "Last name required")
    private String lastName;

    @NotBlank(message = "Email required")
    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "Password required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}