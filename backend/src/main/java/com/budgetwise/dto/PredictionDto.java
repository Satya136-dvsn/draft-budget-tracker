package com.budgetwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal predictedAmount;
    private BigDecimal historicalAverage;
    private Double confidenceScore;
    private String trend; // INCREASING, DECREASING, STABLE
}
