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

    List<Transaction> findBySourceAccountIdOrTargetAccountId(UUID sourceAccountId, UUID targetAccountId);
    
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
    Optional<Transaction> findByExternalTransactionId(String externalId);
    
    List<Transaction> findByStatusAndExpiresAtBefore(Transaction.TransactionStatus status, Instant instant);
}