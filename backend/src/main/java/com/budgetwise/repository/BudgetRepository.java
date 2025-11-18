package com.budgetwise.repository;

import com.budgetwise.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findByUserId(Long userId);
    
    List<Budget> findByUserIdAndPeriod(Long userId, Budget.BudgetPeriod period);
    
    @Query("SELECT b FROM Budget b WHERE b.userId = :userId " +
           "AND b.startDate <= :currentDate AND b.endDate >= :currentDate")
    List<Budget> findActiveBudgets(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT b FROM Budget b WHERE b.userId = :userId " +
           "AND b.categoryId = :categoryId " +
           "AND b.startDate <= :endDate AND b.endDate >= :startDate")
    List<Budget> findOverlappingBudgets(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
    
    Integer countByUserId(Long userId);
}
