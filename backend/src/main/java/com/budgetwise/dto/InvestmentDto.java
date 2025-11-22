package com.budgetwise.dto;

import com.budgetwise.entity.Investment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentDto {

    private Long id;

    @NotBlank(message = "Investment name is required")
    private String name;

    @NotNull(message = "Investment type is required")
    private Investment.InvestmentType type;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Buy price is required")
    @DecimalMin(value = "0.01", message = "Buy price must be greater than 0")
    private BigDecimal buyPrice;

    private BigDecimal currentPrice;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    private String symbol;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Calculated fields for UI
    private BigDecimal totalInvested; // quantity * buyPrice
    private BigDecimal currentValue; // quantity * currentPrice
    private BigDecimal profitLoss; // currentValue - totalInvested
    private BigDecimal profitLossPercent; // (profitLoss / totalInvested) * 100
}
