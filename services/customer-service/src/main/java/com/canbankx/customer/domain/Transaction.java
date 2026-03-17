package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_source_account", columnList = "source_account_id"),
    @Index(name = "idx_target_account", columnList = "target_account_id"),
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_external_id", columnList = "external_transaction_id"),
    @Index(name = "idx_central_bank_id", columnList = "central_bank_transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "source_customer_email")
    private String sourceCustomerEmail;

    @Column(name = "target_account_id")
    private UUID targetAccountId;

    @Column(name = "target_customer_email")
    private String targetCustomerEmail;

    @Column(nullable = false)
    private Integer senderBankId;

    @Column(nullable = false)
    private Integer receiverBankId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "CAD";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(unique = true)
    private String idempotencyKey;

    // Our internal transaction ID (for tracing)
    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    // Central Bank's transaction ID (received from Central Bank)
    @Column(name = "central_bank_transaction_id")
    private String centralBankTransactionId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    // Rejection or expiration reason from Central Bank
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Timestamp when we sent to Central Bank
    @Column(name = "sent_to_central_bank_at")
    private Instant sentToCentralBankAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        expiresAt = Instant.now().plusSeconds(86400); // 24 hours
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAW, TRANSFER, INTERBANK_SEND, INTERBANK_RECEIVE
    }

    public enum TransactionStatus {
        PENDING, PROCESSING, SETTLED, REJECTED, EXPIRED, REFUNDED
    }
}
