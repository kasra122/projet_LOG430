package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "aml_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AMLRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String ruleName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    @Column(nullable = false)
    private BigDecimal threshold;

    @Column(nullable = false)
    private Integer timeWindowMinutes;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public enum RuleType {
        AMOUNT_THRESHOLD, VELOCITY_CHECK, PATTERN_DETECTION, FREQUENCY_LIMIT
    }
}
