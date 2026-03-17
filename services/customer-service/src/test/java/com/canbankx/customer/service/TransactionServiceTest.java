package com.canbankx.customer.service;

import com.canbankx.customer.config.TestDataBuilder;
import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.infrastructure.CentralBankClient;
import com.canbankx.customer.infrastructure.InterBankClient;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.CustomerRepository;
import com.canbankx.customer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InterBankClient interBankClient;

    @Mock
    private CentralBankClient centralBankClient;

    @InjectMocks
    private TransactionService transactionService;

    private UUID customerId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        targetAccountId = UUID.randomUUID();

        sourceAccount = TestDataBuilder.buildAccount(customerId, new BigDecimal("1000.00"));
        sourceAccount.setId(sourceAccountId);

        targetAccount = TestDataBuilder.buildAccount(customerId, new BigDecimal("500.00"));
        targetAccount.setId(targetAccountId);
    }

    @Test
    void testDeposit_Success() {
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.deposit(sourceAccountId, new BigDecimal("100.00"));

        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.SETTLED, result.getStatus());
        assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
        verify(accountRepository, atLeastOnce()).findById(sourceAccountId);
    }

    @Test
    void testWithdraw_Success() {
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.withdraw(sourceAccountId, new BigDecimal("100.00"));

        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.SETTLED, result.getStatus());
        assertEquals(Transaction.TransactionType.WITHDRAW, result.getType());
        verify(accountRepository, atLeastOnce()).findById(sourceAccountId);
    }

    @Test
    void testTransferLocal_Success() {
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccountId)).thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String idempotencyKey = UUID.randomUUID().toString();
        Transaction result = transactionService.transferLocal(sourceAccountId, targetAccountId, new BigDecimal("100.00"), idempotencyKey);

        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.SETTLED, result.getStatus());
        assertEquals(Transaction.TransactionType.TRANSFER, result.getType());
        verify(accountRepository, atLeastOnce()).findById(any(UUID.class));
    }

    @Test
    void testTransferLocal_InsufficientFunds() {
        Account lowBalanceAccount = TestDataBuilder.buildAccount(customerId, new BigDecimal("10.00"));
        lowBalanceAccount.setId(sourceAccountId);

        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(lowBalanceAccount));
        when(accountRepository.findById(targetAccountId)).thenReturn(Optional.of(targetAccount));

        String idempotencyKey = UUID.randomUUID().toString();
        assertThrows(TransactionService.InsufficientFundsException.class, () ->
            transactionService.transferLocal(sourceAccountId, targetAccountId, new BigDecimal("100.00"), idempotencyKey));
    }
}
