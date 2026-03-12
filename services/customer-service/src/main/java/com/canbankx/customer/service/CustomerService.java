package com.canbankx.customer.service;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository repository;
    private final AuditService auditService;

    public Customer createCustomer(Customer customer) {

        customer.setCreatedAt(Instant.now());
        customer.setKycStatus("PENDING");

        Customer saved = repository.save(customer);

        auditService.logAction("CUSTOMER", saved.getId().toString(), "CREATE",
                "Customer registered: " + saved.getEmail(), "SYSTEM");

        log.info("Customer created with KYC status PENDING: {}", saved.getEmail());
        return saved;
    }

    public Customer verifyKyc(UUID customerId) {
        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        customer.setKycStatus("VERIFIED");
        Customer updated = repository.save(customer);

        auditService.logAction("CUSTOMER", customerId.toString(), "KYC_VERIFIED",
                "KYC verification approved", "SYSTEM");

        log.info("KYC verified for customer: {}", customerId);
        return updated;
    }

    public Customer rejectKyc(UUID customerId) {
        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        customer.setKycStatus("REJECTED");
        Customer updated = repository.save(customer);

        auditService.logAction("CUSTOMER", customerId.toString(), "KYC_REJECTED",
                "KYC verification rejected", "SYSTEM");

        log.warn("KYC rejected for customer: {}", customerId);
        return updated;
    }

    public Customer getCustomerById(UUID customerId) {
        return repository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }
}