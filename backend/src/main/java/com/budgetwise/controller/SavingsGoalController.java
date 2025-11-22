package com.budgetwise.controller;

import com.budgetwise.dto.ContributionRequest;
import com.budgetwise.dto.SavingsGoalDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    public ResponseEntity<SavingsGoalDto> createGoal(
            @Valid @RequestBody SavingsGoalDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SavingsGoalDto created = savingsGoalService.createGoal(dto, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoalDto>> getAllGoals(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<SavingsGoalDto> goals = activeOnly ? savingsGoalService.getActiveGoals(userPrincipal.getId())
                : savingsGoalService.getAllGoals(userPrincipal.getId());
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalDto> getGoalById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SavingsGoalDto goal = savingsGoalService.getGoalById(id, userPrincipal.getId());
        return ResponseEntity.ok(goal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalDto> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SavingsGoalDto updated = savingsGoalService.updateGoal(id, dto, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoalDto> addContribution(
            @PathVariable Long id,
            @Valid @RequestBody ContributionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SavingsGoalDto updated = savingsGoalService.addContribution(id, request, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<SavingsGoalDto> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody ContributionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SavingsGoalDto updated = savingsGoalService.withdraw(id, request, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        savingsGoalService.deleteGoal(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
