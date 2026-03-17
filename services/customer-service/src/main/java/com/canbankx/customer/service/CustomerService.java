package com.canbankx.customer.service;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.infrastructure.CentralBankClient;
import com.canbankx.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CentralBankClient centralBankClient;

    @Transactional
    public Customer registerCustomer(String firstName, String lastName, String email) {
        log.info("Registering customer: {}", email);

        if (customerRepository.existsByEmail(email)) {
            throw new CustomerAlreadyExistsException("Customer with email " + email + " already exists");
        }

        Customer customer = Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .kycStatus(Customer.KycStatus.PENDING)
                .bankId(2)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer registered: {}", saved.getId());

        // Register with central bank async
        centralBankClient.registerCustomerWithCentralBank(saved);

        return saved;
    }

    public Optional<Customer> getCustomerById(UUID customerId) {
        return customerRepository.findById(customerId);
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public static class CustomerAlreadyExistsException extends RuntimeException {
        public CustomerAlreadyExistsException(String message) {
            super(message);
        }
    }
}
