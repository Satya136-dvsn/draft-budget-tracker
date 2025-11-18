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
public class CategoryBreakdownDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private Double percentage;
    private Integer transactionCount;
}
