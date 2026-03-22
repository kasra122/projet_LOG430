package com.canbankx.customer.repository;

import com.canbankx.customer.domain.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, UUID> {
    List<BillPayment> findByAccountId(UUID accountId);
    List<BillPayment> findByAccountIdAndStatus(UUID accountId, BillPayment.PaymentStatus status);
    Optional<BillPayment> findByRefNum(String refNum);
    List<BillPayment> findByStatusAndScheduledDateBefore(BillPayment.PaymentStatus status, Instant date);
}
