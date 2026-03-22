package com.canbankx.customer.repository;

import com.canbankx.customer.domain.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatementRepository extends JpaRepository<Statement, UUID> {
    List<Statement> findByAccountId(UUID accountId);
    Optional<Statement> findByAccountIdAndStatementDate(UUID accountId, LocalDate date);
}
