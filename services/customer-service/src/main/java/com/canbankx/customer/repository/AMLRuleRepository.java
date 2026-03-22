package com.canbankx.customer.repository;

import com.canbankx.customer.domain.AMLRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AMLRuleRepository extends JpaRepository<AMLRule, UUID> {
    List<AMLRule> findByActive(Boolean active);
}
