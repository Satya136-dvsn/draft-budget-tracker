package com.budgetwise.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String reportName;

    @Column(nullable = false)
    private String reportType; // "TEMPLATE" or "CUSTOM"

    @Column(columnDefinition = "TEXT")
    private String configuration; // JSON string storing templateId or custom config

    @Column(nullable = false)
    private String frequency; // "WEEKLY", "MONTHLY", "QUARTERLY"

    private LocalDateTime nextRun;

    @Column(nullable = false)
    private String recipients;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
