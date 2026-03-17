package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.dto.AuthRegisterRequest;
import com.canbankx.customer.dto.AuthRegisterResponse;
import com.canbankx.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        log.info("Registering customer: {}", request.getEmail());

        Customer customer = customerService.registerCustomer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
        );

        AuthRegisterResponse response = AuthRegisterResponse.builder()
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .kycStatus(customer.getKycStatus().toString())
                .message("Customer registered successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Bank 2 (Kasra) is healthy");
    }
}
