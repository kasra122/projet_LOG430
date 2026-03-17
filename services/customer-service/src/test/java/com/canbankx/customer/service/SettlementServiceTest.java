package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.SettlementNotificationRequest;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class SettlementServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(transactionRepository, accountRepository);
    }

    @Test
    void testProcessSettledTransfer() {
        // Arrange
        UUID txnId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(txnId)
                .externalTransactionId("TXN-CB-001")
                .senderBankId(2)
                .receiverBankId(1)
                .amount(BigDecimal.valueOf(1000))
                .type(Transaction.TransactionType.INTERBANK_SEND)
                .status(Transaction.TransactionStatus.PROCESSING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        SettlementNotificationRequest notification = SettlementNotificationRequest.builder()
                .externalTransactionId("TXN-CB-001")
                .centralBankTransactionId("CB-TXN-12345")
                .result("SETTLED")
                .settledAt(Instant.now())
                .build();

        when(transactionRepository.findByExternalTransactionId("TXN-CB-001"))
                .thenReturn(Optional.of(transaction));

        // Act
        settlementService.processSettlement(notification);

        // Assert
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        
        Transaction updated = captor.getValue();
        assertEquals(Transaction.TransactionStatus.SETTLED, updated.getStatus());
        assertEquals("CB-TXN-12345", updated.getCentralBankTransactionId());
    }

    @Test
    void testProcessRejectedTransferWithRefund() {
        // Arrange
        UUID txnId = UUID.randomUUID();
        UUID sourceAccountId = UUID.randomUUID();
        
        Transaction transaction = Transaction.builder()
                .id(txnId)
                .externalTransactionId("TXN-CB-002")
                .sourceAccountId(sourceAccountId)
                .senderBankId(2)
                .receiverBankId(1)
                .amount(BigDecimal.valueOf(500))
                .type(Transaction.TransactionType.INTERBANK_SEND)
                .status(Transaction.TransactionStatus.PROCESSING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Account senderAccount = Account.builder()
                .id(sourceAccountId)
                .balance(BigDecimal.ZERO)
                .build();

        SettlementNotificationRequest notification = SettlementNotificationRequest.builder()
                .externalTransactionId("TXN-CB-002")
                .result("REJECTED")
                .reason("Recipient account not found")
                .settledAt(Instant.now())
                .build();

        when(transactionRepository.findByExternalTransactionId("TXN-CB-002"))
                .thenReturn(Optional.of(transaction));
        when(accountRepository.findById(sourceAccountId))
                .thenReturn(Optional.of(senderAccount));

        // Act
        settlementService.processSettlement(notification);

        // Assert
        ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txnCaptor.capture());
        
        Transaction updated = txnCaptor.getValue();
        assertEquals(Transaction.TransactionStatus.REJECTED, updated.getStatus());
        assertEquals("Recipient account not found", updated.getRejectionReason());

        // Verify refund
        ArgumentCaptor<Account> acctCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(acctCaptor.capture());
        
        Account refunded = acctCaptor.getValue();
        assertEquals(BigDecimal.valueOf(500), refunded.getBalance());
    }

    @Test
    void testProcessSettlementForUnknownTransaction() {
        // Arrange
        SettlementNotificationRequest notification = SettlementNotificationRequest.builder()
                .externalTransactionId("TXN-UNKNOWN")
                .result("SETTLED")
                .settledAt(Instant.now())
                .build();

        when(transactionRepository.findByExternalTransactionId("TXN-UNKNOWN"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SettlementService.SettlementException.class,
                () -> settlementService.processSettlement(notification));
    }
}
