package com.canbankx.controller;

import com.canbankx.domain.Customer;
import com.canbankx.dto.LoginRequest;
import com.canbankx.dto.LoginResponse;
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
        try {
            // Verify customer exists
            Customer customer = customerRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // MVP: No password validation, just email verification
            String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    System.currentTimeMillis() + "." +
                    customer.getId().toString().hashCode();

            LoginResponse response = new LoginResponse();
            response.setToken(mockToken);
            response.setCustomerId(customer.getId().toString());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setToken(null);
            errorResponse.setCustomerId(null);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Verify token validity (MVP implementation)
     *
     * @param token Bearer token to verify
     * @return true if token is valid
     */
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }
            // MVP: Basic token validation
            return ResponseEntity.ok(!token.isEmpty());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
    }

    /**
     * Logout endpoint (MVP: stateless, client handles token removal)
     *
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok("Logged out successfully");
    }
}
