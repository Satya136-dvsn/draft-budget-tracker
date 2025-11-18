package com.budgetwise.dto;

import com.budgetwise.entity.Budget;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    
    private Long id;
    
    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    private BigDecimal amount;
    
    private Long categoryId;
    
    private String categoryName;
    
    @NotNull(message = "Budget period is required")
    private Budget.BudgetPeriod period;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @DecimalMin(value = "0", message = "Alert threshold must be between 0 and 100")
    @DecimalMax(value = "100", message = "Alert threshold must be between 0 and 100")
    private BigDecimal alertThreshold;
    
    private BigDecimal spent;
    
    private BigDecimal remaining;
    
    private BigDecimal progressPercentage;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
