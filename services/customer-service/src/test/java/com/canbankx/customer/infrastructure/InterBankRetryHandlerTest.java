package com.canbankx.customer.infrastructure;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterBankRetryHandlerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InterBankClient interBankClient;

    private InterBankRetryHandler retryHandler;

    @BeforeEach
    void setUp() {
        retryHandler = new InterBankRetryHandler(transactionRepository, interBankClient);
    }

    @Test
    void testRetryPendingInterbankTransfers() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        Transaction pendingTransaction = Transaction.builder()
                .id(transactionId)
                .externalTransactionId("EXT-001")
                .sourceCustomerEmail("sender@bank1.com")
                .targetCustomerEmail("recipient@bank2.com")
                .amount(BigDecimal.valueOf(1000))
                .senderBankId(1)
                .receiverBankId(3)
                .currency("CAD")
                .type(Transaction.TransactionType.INTERBANK_SEND)
                .status(Transaction.TransactionStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<Transaction> pendingTransactions = Arrays.asList(pendingTransaction);

        when(transactionRepository.findByStatusAndType(
                Transaction.TransactionStatus.PENDING,
                Transaction.TransactionType.INTERBANK_SEND))
                .thenReturn(pendingTransactions);

        when(interBankClient.sendTransferToBank(any(), any()))
                .thenThrow(new InterBankClient.InterBankCommunicationException("Bank unavailable", new RuntimeException()));

        // Act
        retryHandler.retryPendingInterbankTransfers();

        // Assert
        // When transfer fails, transaction stays PENDING and is not saved
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testNoTransactionsToRetry() {
        // Arrange
        when(transactionRepository.findByStatusAndType(
                Transaction.TransactionStatus.PENDING,
                Transaction.TransactionType.INTERBANK_SEND))
                .thenReturn(Arrays.asList());

        // Act
        retryHandler.retryPendingInterbankTransfers();

        // Assert
        verify(interBankClient, never()).sendTransferToBank(any(), any());
        verify(transactionRepository, never()).save(any());
    }
}
