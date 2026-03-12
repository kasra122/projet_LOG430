package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.dto.LoginRequest;
import com.canbankx.customer.dto.LoginResponse;
import com.canbankx.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Vérifier que le client existe
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Pour MVP: pas de password check, juste existence du client
        return ResponseEntity.ok(new LoginResponse(
            "mock-token-" + customer.getId(),
            customer.getId().toString()
        ));
    }
}