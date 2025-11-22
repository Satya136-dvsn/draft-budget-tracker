package com.budgetwise.repository;

import com.budgetwise.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserIdOrderByPurchaseDateDesc(Long userId);

    List<Investment> findByUserIdAndType(Long userId, Investment.InvestmentType type);

    Optional<Investment> findByIdAndUserId(Long id, Long userId);
}
