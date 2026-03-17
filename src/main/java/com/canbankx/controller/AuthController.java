package com.canbankx.controller;

import com.canbankx.domain.Customer;
import com.canbankx.dto.LoginRequest;
import com.canbankx.dto.LoginResponse;
import com.canbankx.dto.RegisterRequest;
import com.canbankx.exception.InvalidRequestException;
import com.canbankx.exception.UnauthorizedException;
import com.canbankx.repository.CustomerRepository;
import com.canbankx.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new customer account
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("email", "Email cannot be empty");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidRequestException("password", "Password cannot be empty");
        }

        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new InvalidRequestException("firstName", "First name cannot be empty");
        }

        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new InvalidRequestException("lastName", "Last name cannot be empty");
        }

        // Check if customer already exists
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidRequestException("email", "Email already registered");
        }

        // Create new customer
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .kycStatus("PENDING")
                .build();

        customerRepository.save(customer);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(customer.getId(), customer.getEmail());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setCustomerId(customer.getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("email", "Email cannot be empty");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidRequestException("password", "Password cannot be empty");
        }

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(customer.getId(), customer.getEmail());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setCustomerId(customer.getId().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Verify token validity
     */
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authorization header is missing");
        }

        if (!token.startsWith("Bearer ")) {
            throw new InvalidRequestException("Authorization header", "Must start with 'Bearer '");
        }

        String jwt = token.substring(7);
        try {
            jwtTokenProvider.validateTokenAndGetClaims(jwt);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Logout endpoint (stateless - client removes token)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authorization header is missing");
        }

        return ResponseEntity.ok("Logged out successfully");
    }
}
