package com.canbankx.customer.repository;

import com.canbankx.customer.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findBySourceAccountIdOrTargetAccountId(UUID sourceAccountId, UUID targetAccountId);

}