package com.canbankx.customer.repository;

import com.canbankx.customer.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);

    List<Transaction> findBySourceAccountIdOrTargetAccountId(UUID sourceAccountId, UUID targetAccountId);

    List<Transaction> findBySourceAccountId(UUID sourceAccountId);

    List<Transaction> findByTargetAccountId(UUID targetAccountId);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);
}