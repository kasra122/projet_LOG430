package com.canbankx.customer.service;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer createCustomer(Customer customer) {

        customer.setCreatedAt(Instant.now());
        customer.setKycStatus("PENDING");

        return repository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }
}