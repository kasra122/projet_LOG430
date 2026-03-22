package com.canbankx.customer.repository;

import com.canbankx.customer.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {

    Optional<Bank> findByNm(String nm);

    List<Bank> findAllBySt(String st);
}
