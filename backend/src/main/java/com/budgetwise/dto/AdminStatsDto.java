package com.budgetwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    private Long totalUsers;
    private Long totalTransactions;
    private Long totalCategories;
    private Long activeUsers;
    private Long totalPosts;
    private Long totalBudgets;
    private Long totalGoals;
}
