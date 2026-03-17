package com.canbankx.customer.config;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TestDataBuilder {

    // Customer Builders
    public static Customer.CustomerBuilder defaultCustomer() {
        return Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@bank2.com")
                .kycStatus(Customer.KycStatus.VERIFIED)
                .bankId(2);
    }

    public static Customer buildCustomer(String email, Customer.KycStatus kycStatus) {
        return Customer.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email(email)
                .kycStatus(kycStatus)
                .bankId(2)
                .build();
    }

    // Account Builders
    public static Account.AccountBuilder defaultAccount(UUID customerId) {
        return Account.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .accountType(Account.AccountType.CHECKING)
                .currency("CAD")
                .balance(new BigDecimal("1000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    public static Account buildAccount(UUID customerId, BigDecimal balance) {
        return Account.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .accountType(Account.AccountType.CHECKING)
                .currency("CAD")
                .balance(balance)
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // Transaction Builders
    public static Transaction.TransactionBuilder defaultTransaction() {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("500.00"))
                .currency("CAD")
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SETTLED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    public static Transaction buildLocalTransfer(UUID sourceId, UUID targetId) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(sourceId)
                .targetAccountId(targetId)
                .senderBankId(2)
                .receiverBankId(2)
                .amount(new BigDecimal("500.00"))
                .currency("CAD")
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SETTLED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .settledAt(Instant.now())
                .build();
    }

    public static Transaction buildInterbankTransfer(UUID sourceId, String sourceEmail, String targetEmail) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(sourceId)
                .sourceCustomerEmail(sourceEmail)
                .targetCustomerEmail(targetEmail)
                .senderBankId(2)
                .receiverBankId(1)
                .amount(new BigDecimal("250.00"))
                .currency("CAD")
                .type(Transaction.TransactionType.INTERBANK_SEND)
                .status(Transaction.TransactionStatus.PENDING)
                .idempotencyKey(UUID.randomUUID().toString())
                .externalTransactionId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();
    }

    // DTO Builders
    public static AuthRegisterRequest buildRegisterRequest(String email) {
        return AuthRegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("TestPassword123")
                .build();
    }

    public static TransferRequest buildTransferRequest(UUID sourceId, String targetEmail, Integer bankId) {
        return TransferRequest.builder()
                .sourceAccountId(sourceId.toString())
                .targetCustomerEmail(targetEmail)
                .amount(new BigDecimal("100.00"))
                .receiverBankId(bankId)
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
    }

    public static InboundTransferRequest buildInboundTransferRequest(String recipientEmail) {
        return InboundTransferRequest.builder()
                .externalTransactionId(UUID.randomUUID().toString())
                .senderCustomerEmail("sender@bank1.com")
                .recipientEmail(recipientEmail)
                .amount(new BigDecimal("150.00"))
                .senderBankId(1)
                .currency("CAD")
                .build();
    }

    public static SettlementNotificationRequest buildSettlementNotification(String externalTxId, String result) {
        return SettlementNotificationRequest.builder()
                .externalTransactionId(externalTxId)
                .result(result)
                .reason("Test settlement")
                .build();
    }
}
