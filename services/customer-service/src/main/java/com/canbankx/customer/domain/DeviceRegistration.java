package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_registrations", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private String ipAddr;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceStatus status = DeviceStatus.PENDING;

    @Column(nullable = false)
    private Instant registeredAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "trusted", nullable = false)
    private Boolean isTrusted = false;

    @PrePersist
    protected void onCreate() {
        registeredAt = Instant.now();
    }

    public enum DeviceStatus {
        PENDING, VERIFIED, TRUSTED, REVOKED
    }
   
}
