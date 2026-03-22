package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_tokens", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_token", columnList = "token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTPToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OTPStatus status = OTPStatus.PENDING;

    @Column(name = "delivery_method", nullable = false)
    private String deliveryMethod;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCnt = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        expiresAt = Instant.now().plusSeconds(300);
    }

    public enum OTPStatus {
        PENDING, VERIFIED, EXPIRED, LOCKED
    }
}