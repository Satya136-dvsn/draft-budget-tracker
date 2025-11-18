package com.budgetwise.controller;

import com.budgetwise.dto.CategoryBreakdownDto;
import com.budgetwise.dto.DashboardSummaryDto;
import com.budgetwise.dto.MonthlyTrendDto;
import com.budgetwise.dto.TransactionDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(userPrincipal.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrends(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false, defaultValue = "6") Integer months) {
        List<MonthlyTrendDto> trends = dashboardService.getMonthlyTrends(userPrincipal.getId(), months);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CategoryBreakdownDto> breakdown = dashboardService.getCategoryBreakdown(userPrincipal.getId());
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<List<TransactionDto>> getRecentTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<TransactionDto> transactions = dashboardService.getRecentTransactions(userPrincipal.getId(), limit);
        return ResponseEntity.ok(transactions);
    }
}
