package com.canbankx.controller;

import com.canbankx.domain.Customer;
import com.canbankx.dto.SignupRequest;
import com.canbankx.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping("/signup")
    public ResponseEntity<Customer> signup(@RequestBody SignupRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .kycStatus("PENDING")  // KYC verification pending
                .createdAt(Instant.now())
                .build();

        // Simulate KYC verification (always approve for MVP)
        customer.setKycStatus("ACTIVE");

        Customer saved = service.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        customer.setKycStatus("PENDING");
        customer.setCreatedAt(Instant.now());
        Customer saved = service.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers() {
        return ResponseEntity.ok(service.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable UUID id) {
        return service.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}