package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "statements", indexes = {
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_statement_date", columnList = "statement_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal closingBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDebits;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCredits;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @Column(name = "transaction_count", nullable = false)
    private Integer txnCount = 0;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
