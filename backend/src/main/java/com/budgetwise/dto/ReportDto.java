package com.budgetwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

public class ReportDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduledReportDto {
        private Long id;
        private String name;
        private String frequency; // Weekly, Monthly, Quarterly
        private String nextRun;
        private String recipients;
        private String status; // Active, Paused
        private String reportType;
        private String configuration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomReportConfig {
        private String name;
        private String dateRange; // this_month, last_month, last_quarter, last_year, custom
        private Map<String, Boolean> metrics; // income, expenses, savings, budgets, goals
        private boolean preview;
    }
}
