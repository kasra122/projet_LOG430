package com.canbankx.customer.repository;

import com.canbankx.customer.domain.Payee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayeeRepository extends JpaRepository<Payee, UUID> {
    List<Payee> findByCustomerId(UUID customerId);
}
