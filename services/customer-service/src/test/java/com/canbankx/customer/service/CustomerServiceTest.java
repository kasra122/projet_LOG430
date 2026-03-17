package com.canbankx.customer.service;

import com.canbankx.customer.config.TestDataBuilder;
import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.infrastructure.CentralBankClient;
import com.canbankx.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CentralBankClient centralBankClient;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRegisterCustomer_Success() {
        String email = "newuser@bank2.com";
        Customer newCustomer = TestDataBuilder.buildCustomer(email, Customer.KycStatus.PENDING);

        when(customerRepository.existsByEmail(email)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(newCustomer);

        Customer result = customerService.registerCustomer("John", "Doe", email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Customer.KycStatus.PENDING, result.getKycStatus());
        verify(customerRepository).save(any(Customer.class));
        // TODO: Verify central bank registration when endpoint is available
    }

    @Test
    void testRegisterCustomer_DuplicateEmail() {
        String email = "existing@bank2.com";
        when(customerRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(CustomerService.CustomerAlreadyExistsException.class,
            () -> customerService.registerCustomer("John", "Doe", email));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testGetCustomerById_Found() {
        UUID customerId = UUID.randomUUID();
        Customer customer = TestDataBuilder.buildCustomer("john@bank2.com", Customer.KycStatus.VERIFIED);
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerById(customerId);

        assertTrue(result.isPresent());
        assertEquals(customerId, result.get().getId());
    }

    @Test
    void testGetCustomerById_NotFound() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerById(customerId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCustomerByEmail_Found() {
        String email = "john@bank2.com";
        Customer customer = TestDataBuilder.buildCustomer(email, Customer.KycStatus.VERIFIED);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    void testGetCustomerByEmail_NotFound() {
        String email = "unknown@bank2.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerByEmail(email);

        assertTrue(result.isEmpty());
    }
}
