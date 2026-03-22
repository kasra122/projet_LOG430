package com.canbankx.customer.repository;

import com.canbankx.customer.domain.OTPToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OTPTokenRepository extends JpaRepository<OTPToken, UUID> {
    Optional<OTPToken> findByToken(String token);
    Optional<OTPToken> findByCustomerIdAndStatus(UUID customerId, OTPToken.OTPStatus status);
}
