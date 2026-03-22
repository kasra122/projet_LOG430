package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bill_payments", indexes = {
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_payee_id", columnList = "payee_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "payee_id", nullable = false)
    private UUID payeeId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "CAD";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "reference_number", unique = true)
    private String refNum;

    @Column(name = "scheduled_date")
    private Instant scheduledDate;

    @Column(name = "executed_date")
    private Instant executedDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum PaymentStatus {
        PENDING, SCHEDULED, PROCESSING, COMPLETED, FAILED, CANCELLED
    }

    public enum PaymentType {
        BILL, TRANSFER, UTILITY
    }
}
