package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer management and KYC (UC-01)")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a new customer with KYC verification (UC-01)")
    public Customer createCustomer(@Valid @RequestBody Customer customer) {
        return service.createCustomer(customer);
    }

    @GetMapping
    @Operation(summary = "List all customers")
    public List<Customer> getCustomers() {
        return service.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID")
    public Customer getCustomer(@PathVariable UUID customerId) {
        return service.getCustomerById(customerId);
    }

    @PutMapping("/{customerId}/kyc/verify")
    @Operation(summary = "Verify customer KYC status (UC-01)")
    public Customer verifyKyc(@PathVariable UUID customerId) {
        return service.verifyKyc(customerId);
    }

    @PutMapping("/{customerId}/kyc/reject")
    @Operation(summary = "Reject customer KYC (UC-01)")
    public Customer rejectKyc(@PathVariable UUID customerId) {
        return service.rejectKyc(customerId);
    }

}