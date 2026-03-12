package com.canbankx.controller;

import com.canbankx.domain.Customer;
import com.canbankx.dto.LoginRequest;
import com.canbankx.dto.LoginResponse;
import com.canbankx.exception.InvalidRequestException;
import com.canbankx.exception.UnauthorizedException;
import com.canbankx.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;

    /**
     * Login endpoint for customer authentication
     * MVP: Basic email verification without password
     *
     * @param request LoginRequest containing email and password
     * @return LoginResponse with mock token and customer ID
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("email", "Email cannot be empty");
        }

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found with email: " + request.getEmail()));

        // MVP: No password validation, just email verification
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                System.currentTimeMillis() + "." +
                customer.getId().toString().hashCode();

        LoginResponse response = new LoginResponse();
        response.setToken(mockToken);
        response.setCustomerId(customer.getId().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Verify token validity (MVP implementation)
     *
     * @param token Bearer token to verify
     * @return true if token is valid
     */
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authorization header is missing");
        }

        if (!token.startsWith("Bearer ")) {
            throw new InvalidRequestException("Authorization header", "Must start with 'Bearer '");
        }

        return ResponseEntity.ok(true);
    }

    /**
     * Logout endpoint (MVP: stateless, client handles token removal)
     *
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authorization header is missing");
        }

        return ResponseEntity.ok("Logged out successfully");
    }
}