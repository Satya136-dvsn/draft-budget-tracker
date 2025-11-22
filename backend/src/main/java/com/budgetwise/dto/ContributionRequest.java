package com.budgetwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContributionRequest {
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
}
