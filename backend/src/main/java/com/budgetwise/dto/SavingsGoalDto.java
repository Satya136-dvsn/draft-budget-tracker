package com.budgetwise.dto;

import com.budgetwise.entity.SavingsGoal;
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
public class SavingsGoalDto {
    
    private Long id;
    
    @NotBlank(message = "Goal name is required")
    @Size(max = 255, message = "Goal name cannot exceed 255 characters")
    private String name;
    
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;
    
    private BigDecimal currentAmount;
    
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;
    
    private SavingsGoal.GoalStatus status;
    
    private BigDecimal progressPercentage;
    
    private BigDecimal requiredMonthlySavings;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
