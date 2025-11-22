package com.budgetwise.repository;

import com.budgetwise.entity.ScheduledReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, Long> {
    List<ScheduledReport> findByUserId(Long userId);

    List<ScheduledReport> findByActiveTrue();
}
