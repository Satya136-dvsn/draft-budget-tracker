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
public class BudgetAdviceDto {
    private String category;
    private String recommendation;
    private BigDecimal currentSpending;
    private BigDecimal recommendedSpending;
    private Double percentageOfIncome;
    private String priority; // HIGH, MEDIUM, LOW
    private String actionItem;
}
