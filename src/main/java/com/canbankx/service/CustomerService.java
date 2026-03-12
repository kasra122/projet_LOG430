package com.canbankx.service;

import com.canbankx.domain.Customer;
import com.canbankx.exception.InvalidRequestException;
import com.canbankx.exception.ResourceNotFoundException;
import com.canbankx.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public Customer createCustomer(Customer customer) {
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new InvalidRequestException("email", "Email cannot be empty");
        }

        if (customer.getFirstName() == null || customer.getFirstName().isBlank()) {
            throw new InvalidRequestException("firstName", "First name cannot be empty");
        }

        if (customer.getLastName() == null || customer.getLastName().isBlank()) {
            throw new InvalidRequestException("lastName", "Last name cannot be empty");
        }

        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(Instant.now());
        }
        if (customer.getKycStatus() == null) {
            customer.setKycStatus("PENDING");
        }

        return repository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    public Customer getCustomerById(UUID id) {
        if (id == null) {
            throw new InvalidRequestException("id", "Customer ID cannot be null");
        }

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public Customer getCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidRequestException("email", "Email cannot be empty");
        }

        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }
}