package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.exception.InsufficientFundsException;
import com.canbankx.customer.exception.InvalidAmountException;
import com.canbankx.customer.exception.ResourceNotFoundException;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.TransactionRepository;
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
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AmlService amlService;

    @InjectMocks
    private TransactionService transactionService;

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
    void deposit_Success() {
        UUID accountId = testAccount.getId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        Transaction result = transactionService.deposit(accountId, new BigDecimal("500"), null);

        assertEquals("DEPOSIT", result.getType());
        assertEquals(new BigDecimal("500"), result.getAmount());
        assertEquals(new BigDecimal("1500.00"), testAccount.getBalance());
        verify(amlService).checkTransaction(any());
    }

    @Test
    void deposit_WithIdempotencyKey_ReturnsSameTransaction() {
        String key = "test-key-123";
        Transaction existing = Transaction.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(testAccount.getId())
                .amount(new BigDecimal("100"))
                .type("DEPOSIT")
                .idempotencyKey(key)
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        Transaction result = transactionService.deposit(testAccount.getId(), new BigDecimal("100"), key);

        assertEquals(existing.getId(), result.getId());
        verify(accountRepository, never()).findById(any());
    }

    @Test
    void deposit_NegativeAmount_ThrowsException() {
        assertThrows(InvalidAmountException.class,
                () -> transactionService.deposit(testAccount.getId(), new BigDecimal("-100"), null));
    }

    @Test
    void deposit_ZeroAmount_ThrowsException() {
        assertThrows(InvalidAmountException.class,
                () -> transactionService.deposit(testAccount.getId(), BigDecimal.ZERO, null));
    }

    @Test
    void deposit_AccountNotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deposit(id, new BigDecimal("100"), null));
    }

    @Test
    void withdraw_Success() {
        UUID accountId = testAccount.getId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        Transaction result = transactionService.withdraw(accountId, new BigDecimal("300"), null);

        assertEquals("WITHDRAW", result.getType());
        assertEquals(new BigDecimal("700.00"), testAccount.getBalance());
    }

    @Test
    void withdraw_InsufficientFunds_ThrowsException() {
        UUID accountId = testAccount.getId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.withdraw(accountId, new BigDecimal("5000"), null));
    }

    @Test
    void transfer_Success() {
        Account target = Account.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .accountType("SAVINGS")
                .currency("CAD")
                .balance(new BigDecimal("200.00"))
                .createdAt(Instant.now())
                .build();

        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        Transaction result = transactionService.transfer(
                testAccount.getId(), target.getId(), new BigDecimal("250"), null);

        assertEquals("TRANSFER", result.getType());
        assertEquals(new BigDecimal("750.00"), testAccount.getBalance());
        assertEquals(new BigDecimal("450.00"), target.getBalance());
    }

    @Test
    void transfer_SameAccount_ThrowsException() {
        UUID accountId = testAccount.getId();

        assertThrows(InvalidAmountException.class,
                () -> transactionService.transfer(accountId, accountId, new BigDecimal("100"), null));
    }

    @Test
    void transfer_InsufficientFunds_ThrowsException() {
        Account target = Account.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal("0"))
                .build();

        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.transfer(testAccount.getId(), target.getId(), new BigDecimal("5000"), null));
    }
}
