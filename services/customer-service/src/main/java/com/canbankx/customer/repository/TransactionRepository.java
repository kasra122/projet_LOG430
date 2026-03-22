package com.canbankx.customer.repository;

import com.canbankx.customer.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findBySourceAccountId(UUID accountId);
    List<Transaction> findBySourceAccountIdAndCreatedAtAfter(UUID accountId, Instant date);
    List<Transaction> findBySourceAccountIdAndCreatedAtBetween(UUID accountId, Instant start, Instant end);
    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);
    List<Transaction> findByStatusAndType(Transaction.TransactionStatus status, Transaction.TransactionType type);
}
