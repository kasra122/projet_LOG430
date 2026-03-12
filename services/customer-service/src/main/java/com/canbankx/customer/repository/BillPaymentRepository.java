package com.canbankx.customer.repository;

import com.canbankx.customer.domain.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillPaymentRepository extends JpaRepository<BillPayment, UUID> {

    List<BillPayment> findByAccountId(UUID accountId);

    Optional<BillPayment> findByIdempotencyKey(String idempotencyKey);
}
