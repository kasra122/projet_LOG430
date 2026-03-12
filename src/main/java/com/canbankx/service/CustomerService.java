package com.canbankx.service;

import com.canbankx.domain.Customer;
import com.canbankx.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public Customer createCustomer(Customer customer) {
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

    public Optional<Customer> getCustomerById(UUID id) {
        return repository.findById(id);
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return repository.findByEmail(email);
    }
}