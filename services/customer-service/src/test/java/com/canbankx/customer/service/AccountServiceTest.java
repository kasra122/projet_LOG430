package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_CheckingType_Success() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        Account result = accountService.createAccount(customerId, "CHECKING", "CAD");

        assertEquals("CHECKING", result.getAccountType());
        assertEquals("CAD", result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(auditService).logAction(eq("ACCOUNT"), any(), eq("CREATE"), any(), eq(customerId.toString()));
    }

    @Test
    void createAccount_SavingsType_Success() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        Account result = accountService.createAccount(customerId, "SAVINGS", "CAD");
        assertEquals("SAVINGS", result.getAccountType());
    }

    @Test
    void createAccount_InvalidType_ThrowsException() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.existsById(customerId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount(customerId, "INVALID", "CAD"));
    }

    @Test
    void createAccount_CustomerNotFound_ThrowsException() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.createAccount(customerId, "CHECKING", "CAD"));
    }

    @Test
    void getAccountsByCustomer_ReturnsList() {
        UUID customerId = UUID.randomUUID();
        when(accountRepository.findByCustomerId(customerId)).thenReturn(List.of());

        List<Account> results = accountService.getAccountsByCustomer(customerId);
        assertNotNull(results);
    }

    @Test
    void getAllAccounts_ReturnsList() {
        when(accountRepository.findAll()).thenReturn(List.of());

        List<Account> results = accountService.getAllAccounts();
        assertNotNull(results);
    }
}
