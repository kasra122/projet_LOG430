package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.BillPayment;
import com.canbankx.customer.exception.InsufficientFundsException;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.BillPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillPaymentServiceTest {

    @Mock
    private BillPaymentRepository billPaymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BillPaymentService billPaymentService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .accountType("CHECKING")
                .currency("CAD")
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void payBill_Success() {
        UUID accountId = testAccount.getId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(billPaymentRepository.save(any(BillPayment.class))).thenAnswer(invocation -> {
            BillPayment bp = invocation.getArgument(0);
            bp.setId(UUID.randomUUID());
            return bp;
        });

        BillPayment result = billPaymentService.payBill(accountId, "Hydro-Quebec", new BigDecimal("150"), null);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals("Hydro-Quebec", result.getPayee());
        assertEquals(new BigDecimal("850.00"), testAccount.getBalance());
    }

    @Test
    void payBill_WithIdempotencyKey_ReturnsSame() {
        String key = "bill-key-123";
        BillPayment existing = BillPayment.builder()
                .id(UUID.randomUUID())
                .accountId(testAccount.getId())
                .payee("Bell")
                .amount(new BigDecimal("100"))
                .status("COMPLETED")
                .idempotencyKey(key)
                .createdAt(Instant.now())
                .build();

        when(billPaymentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        BillPayment result = billPaymentService.payBill(testAccount.getId(), "Bell", new BigDecimal("100"), key);

        assertEquals(existing.getId(), result.getId());
        verify(accountRepository, never()).findById(any());
    }

    @Test
    void payBill_InsufficientFunds_ThrowsException() {
        UUID accountId = testAccount.getId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientFundsException.class,
                () -> billPaymentService.payBill(accountId, "Hydro-Quebec", new BigDecimal("5000"), null));
    }

    @Test
    void payBill_AccountNotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> billPaymentService.payBill(id, "Bell", new BigDecimal("100"), null));
    }
}
