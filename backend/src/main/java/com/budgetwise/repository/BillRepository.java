package com.budgetwise.repository;

import com.budgetwise.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByUserIdOrderByNextDueDateAsc(Long userId);

    List<Bill> findByUserIdAndStatus(Long userId, Bill.BillStatus status);

    List<Bill> findByUserIdAndNextDueDateBetween(Long userId, LocalDate start, LocalDate end);

    Optional<Bill> findByIdAndUserId(Long id, Long userId);

    List<Bill> findByUserIdAndNextDueDateBeforeAndStatus(Long userId, LocalDate date, Bill.BillStatus status);
}
