package com.canbankx.customer.repository;

import com.canbankx.customer.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findBySessionToken(String sessionToken);
    List<Session> findByCustomerId(UUID customerId);
    List<Session> findByCustomerIdAndStatus(UUID customerId, Session.SessionStatus status);
}
