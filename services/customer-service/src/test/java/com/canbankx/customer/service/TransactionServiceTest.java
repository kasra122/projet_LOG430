package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.CentralBankTransferResponse;
import com.canbankx.customer.infrastructure.CentralBankClient;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CentralBankClient centralBankClient;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, accountRepository, centralBankClient);
    }

    @Test
    void testInitiateInterbankTransfer_Success() {
        // Arrange
        UUID senderAccountId = UUID.randomUUID();
        Account senderAccount = Account.builder()
                .id(senderAccountId)
                .balance(BigDecimal.valueOf(5000))
                .build();

        when(accountRepository.findById(senderAccountId))
                .thenReturn(Optional.of(senderAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CentralBankTransferResponse cbResponse = CentralBankTransferResponse.builder()
                .externalTransactionId("TXN-ABC12345")
                .centralBankTransactionId("CB-TXN-99999")
                .status("ACCEPTED")
                .processedAt(Instant.now())
                .build();

        when(centralBankClient.sendTransferRequest(any()))
                .thenReturn(cbResponse);

        // Act
        Transaction result = transactionService.initiateInterbankTransfer(
                senderAccountId,
                "john@bank2.com",
                "jane@bank1.com",
                1,
                BigDecimal.valueOf(1000),
                "CAD"
        );

        // Assert
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.PROCESSING, result.getStatus());
        assertEquals("CB-TXN-99999", result.getCentralBankTransactionId());
        verify(accountRepository, atLeastOnce()).save(any(Account.class)); // At least once for deduction
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class)); // At least once for transaction
    }

    @Test
    void testInitiateInterbankTransfer_InsufficientBalance() {
        // Arrange
        UUID senderAccountId = UUID.randomUUID();
        Account senderAccount = Account.builder()
                .id(senderAccountId)
                .balance(BigDecimal.valueOf(100)) // Only 100, trying to send 1000
                .build();

        when(accountRepository.findById(senderAccountId))
                .thenReturn(Optional.of(senderAccount));

        // Act & Assert
        assertThrows(TransactionService.TransactionException.class,
                () -> transactionService.initiateInterbankTransfer(
                        senderAccountId,
                        "john@bank2.com",
                        "jane@bank1.com",
                        1,
                        BigDecimal.valueOf(1000),
                        "CAD"
                ));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testInitiateInterbankTransfer_AccountNotFound() {
        // Arrange
        UUID senderAccountId = UUID.randomUUID();
        when(accountRepository.findById(senderAccountId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TransactionService.TransactionException.class,
                () -> transactionService.initiateInterbankTransfer(
                        senderAccountId,
                        "john@bank2.com",
                        "jane@bank1.com",
                        1,
                        BigDecimal.valueOf(1000),
                        "CAD"
                ));
    }

    @Test
    void testInitiateInterbankTransfer_CentralBankError() {
        // Arrange
        UUID senderAccountId = UUID.randomUUID();
        Account senderAccount = Account.builder()
                .id(senderAccountId)
                .balance(BigDecimal.valueOf(5000))
                .build();

        when(accountRepository.findById(senderAccountId))
                .thenReturn(Optional.of(senderAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(centralBankClient.sendTransferRequest(any()))
                .thenThrow(new CentralBankClient.CentralBankCommunicationException("Connection failed", new RuntimeException()));

        // Act & Assert
        assertThrows(TransactionService.TransactionException.class,
                () -> transactionService.initiateInterbankTransfer(
                        senderAccountId,
                        "john@bank2.com",
                        "jane@bank1.com",
                        1,
                        BigDecimal.valueOf(1000),
                        "CAD"
                ));

        // Verify refund was issued
        verify(accountRepository, atLeastOnce()).save(any(Account.class));
    }

    @Test
    void testGetTransaction() {
        // Arrange
        UUID txnId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(txnId)
                .externalTransactionId("TXN-ABC12345")
                .status(Transaction.TransactionStatus.SETTLED)
                .build();

        when(transactionRepository.findById(txnId))
                .thenReturn(Optional.of(transaction));

        // Act
        Optional<Transaction> result = transactionService.getTransaction(txnId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(txnId, result.get().getId());
    }

    @Test
    void testGetTransactionByExternalId() {
        // Arrange
        String externalId = "TXN-ABC12345";
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .externalTransactionId(externalId)
                .status(Transaction.TransactionStatus.SETTLED)
                .build();

        when(transactionRepository.findByExternalTransactionId(externalId))
                .thenReturn(Optional.of(transaction));

        // Act
        Optional<Transaction> result = transactionService.getTransactionByExternalId(externalId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(externalId, result.get().getExternalTransactionId());
    }
}
