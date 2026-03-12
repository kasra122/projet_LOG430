package com.canbankx.customer.service;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
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
    private AuditService auditService;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("hashedpassword")
                .kycStatus("PENDING")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void createCustomer_SetsKycStatusToPending() {
        Customer input = Customer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .password("hashedpassword")
                .build();

        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Customer result = customerService.createCustomer(input);

        assertEquals("PENDING", result.getKycStatus());
        assertNotNull(result.getCreatedAt());
        verify(auditService).logAction(eq("CUSTOMER"), any(), eq("CREATE"), any(), eq("SYSTEM"));
    }

    @Test
    void verifyKyc_UpdatesStatusToVerified() {
        UUID customerId = testCustomer.getId();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer result = customerService.verifyKyc(customerId);

        assertEquals("VERIFIED", result.getKycStatus());
        verify(auditService).logAction(eq("CUSTOMER"), eq(customerId.toString()), eq("KYC_VERIFIED"), any(), eq("SYSTEM"));
    }

    @Test
    void rejectKyc_UpdatesStatusToRejected() {
        UUID customerId = testCustomer.getId();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer result = customerService.rejectKyc(customerId);

        assertEquals("REJECTED", result.getKycStatus());
        verify(auditService).logAction(eq("CUSTOMER"), eq(customerId.toString()), eq("KYC_REJECTED"), any(), eq("SYSTEM"));
    }

    @Test
    void verifyKyc_ThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.verifyKyc(id));
    }

    @Test
    void getCustomerById_ReturnsCustomer() {
        UUID id = testCustomer.getId();
        when(customerRepository.findById(id)).thenReturn(Optional.of(testCustomer));

        Customer result = customerService.getCustomerById(id);
        assertEquals(testCustomer.getEmail(), result.getEmail());
    }

    @Test
    void getCustomerById_ThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(id));
    }

    @Test
    void getAllCustomers_ReturnsList() {
        when(customerRepository.findAll()).thenReturn(List.of(testCustomer));

        List<Customer> results = customerService.getAllCustomers();
        assertEquals(1, results.size());
    }
}
