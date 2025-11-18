package com.budgetwise.controller;

import com.budgetwise.dto.BudgetDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {
    
    private final BudgetService budgetService;
    
    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(
            @Valid @RequestBody BudgetDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BudgetDto created = budgetService.createBudget(dto, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    public ResponseEntity<List<BudgetDto>> getAllBudgets(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<BudgetDto> budgets = activeOnly ? 
            budgetService.getActiveBudgets(userPrincipal.getId()) :
            budgetService.getAllBudgets(userPrincipal.getId());
        return ResponseEntity.ok(budgets);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudgetById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BudgetDto budget = budgetService.getBudgetById(id, userPrincipal.getId());
        return ResponseEntity.ok(budget);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BudgetDto updated = budgetService.updateBudget(id, dto, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        budgetService.deleteBudget(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<List<BudgetDto>> getBudgetAlerts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<BudgetDto> budgets = budgetService.getActiveBudgets(userPrincipal.getId());
        
        // Filter budgets that exceed alert threshold
        List<BudgetDto> alerts = budgets.stream()
            .filter(b -> b.getProgressPercentage().compareTo(b.getAlertThreshold()) >= 0)
            .toList();
        
        return ResponseEntity.ok(alerts);
    }
}
