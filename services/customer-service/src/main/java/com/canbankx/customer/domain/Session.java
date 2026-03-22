package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_session_token", columnList = "session_token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(nullable = false)
    private String ipAddr;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(name = "last_activity")
    private Instant lastActivity;

    @Column(name = "mfa_verified", nullable = false)
    private Boolean mfaVerified = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        lastActivity = Instant.now();
        expiresAt = Instant.now().plusSeconds(3600);
    }

    public enum SessionStatus {
        ACTIVE, EXPIRED, REVOKED, LOCKED
    }
}