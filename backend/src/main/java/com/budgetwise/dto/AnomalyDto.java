package com.budgetwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDto {
    private Long transactionId;
    private String description;
    private BigDecimal amount;
    private String categoryName;
    private LocalDate date;
    private BigDecimal categoryAverage;
    private BigDecimal standardDeviation;
    private Double zScore;
    private String severity; // LOW, MEDIUM, HIGH
    private String reason;
}
