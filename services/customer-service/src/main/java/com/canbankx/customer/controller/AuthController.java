package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.dto.AuthRequest;
import com.canbankx.customer.dto.AuthResponse;
import com.canbankx.customer.dto.RegisterRequest;
import com.canbankx.customer.repository.CustomerRepository;
import com.canbankx.customer.security.JwtTokenProvider;
import com.canbankx.customer.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration and JWT authentication (UC-01, UC-02)")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer (UC-01)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .phone(request.getPhone())
                .kycStatus("PENDING")
                .createdAt(Instant.now())
                .build();

        Customer saved = customerRepository.save(customer);

        auditService.logAction("CUSTOMER", saved.getId().toString(), "REGISTER",
                "Customer registered: " + saved.getEmail(), "SYSTEM");

        String token = jwtTokenProvider.generateToken(saved.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, saved.getEmail()));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT token (UC-02)")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(customer.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, customer.getEmail()));
    }
}
