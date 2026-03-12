package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.dto.AuthRequest;
import com.canbankx.customer.dto.AuthResponse;
import com.canbankx.customer.repository.CustomerRepository;
import com.canbankx.customer.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT token")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String token = jwtTokenProvider.generateToken(customer.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, customer.getEmail()));
    }
}
