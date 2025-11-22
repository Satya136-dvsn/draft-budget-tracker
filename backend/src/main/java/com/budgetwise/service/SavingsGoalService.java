package com.budgetwise.service;

import com.budgetwise.dto.ContributionRequest;
import com.budgetwise.dto.SavingsGoalDto;
import com.budgetwise.entity.SavingsGoal;
import com.budgetwise.exception.ResourceNotFoundException;
import com.budgetwise.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final com.budgetwise.repository.TransactionRepository transactionRepository;

    @Transactional
    public SavingsGoalDto createGoal(SavingsGoalDto dto, Long userId) {
        // Validate deadline is in future
        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }

        SavingsGoal goal = new SavingsGoal();
        goal.setUserId(userId);
        goal.setName(dto.getName());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setCurrentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : BigDecimal.ZERO);
        goal.setDeadline(dto.getDeadline());
        goal.setStatus(SavingsGoal.GoalStatus.ACTIVE);

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return mapToDto(saved);
    }

    public List<SavingsGoalDto> getAllGoals(Long userId) {
        return savingsGoalRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SavingsGoalDto> getActiveGoals(Long userId) {
        return savingsGoalRepository.findByUserIdAndStatus(userId, SavingsGoal.GoalStatus.ACTIVE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public SavingsGoalDto getGoalById(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        return mapToDto(goal);
    }

    @Transactional
    public SavingsGoalDto updateGoal(Long id, SavingsGoalDto dto, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));

        // Validate deadline is in future
        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }

        goal.setName(dto.getName());
        goal.setTargetAmount(dto.getTargetAmount());
        if (dto.getCurrentAmount() != null) {
            goal.setCurrentAmount(dto.getCurrentAmount());
        }
        goal.setDeadline(dto.getDeadline());

        // Check if goal is completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.COMPLETED);
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = { "dashboard_summary", "dashboard_trends", "dashboard_breakdown" }, allEntries = true)
    public SavingsGoalDto addContribution(Long id, ContributionRequest request, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));

        if (goal.getStatus() != SavingsGoal.GoalStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot contribute to inactive goal");
        }

        BigDecimal newAmount = goal.getCurrentAmount().add(request.getAmount());
        goal.setCurrentAmount(newAmount);

        // Check if goal is completed
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.COMPLETED);
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);

        // Create transaction for the contribution
        com.budgetwise.entity.Transaction transaction = new com.budgetwise.entity.Transaction();
        transaction.setUserId(userId);
        transaction.setType(com.budgetwise.entity.Transaction.TransactionType.EXPENSE);
        transaction.setAmount(request.getAmount());
        transaction.setCategoryId(null);
        transaction.setDescription("Savings Goal Contribution: " + goal.getName());
        transaction.setTransactionDate(LocalDate.now());
        transaction.setIsAnomaly(false);

        transactionRepository.save(transaction);

        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = { "dashboard_summary", "dashboard_trends", "dashboard_breakdown" }, allEntries = true)
    public SavingsGoalDto withdraw(Long id, ContributionRequest request, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));

        if (goal.getCurrentAmount().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in savings goal");
        }

        BigDecimal newAmount = goal.getCurrentAmount().subtract(request.getAmount());
        goal.setCurrentAmount(newAmount);

        // If goal was completed but now isn't, revert status to ACTIVE
        if (goal.getStatus() == SavingsGoal.GoalStatus.COMPLETED && newAmount.compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(SavingsGoal.GoalStatus.ACTIVE);
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);

        // Create transaction for the withdrawal (INCOME as money comes back to user)
        com.budgetwise.entity.Transaction transaction = new com.budgetwise.entity.Transaction();
        transaction.setUserId(userId);
        transaction.setType(com.budgetwise.entity.Transaction.TransactionType.INCOME);
        transaction.setAmount(request.getAmount());
        transaction.setCategoryId(null);
        transaction.setDescription("Withdrawal from Savings Goal: " + goal.getName());
        transaction.setTransactionDate(LocalDate.now());
        transaction.setIsAnomaly(false);

        transactionRepository.save(transaction);

        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = { "dashboard_summary", "dashboard_trends", "dashboard_breakdown" }, allEntries = true)
    public void deleteGoal(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));

        // Find all transactions related to this goal and convert them to INCOME
        // This returns the money to the user's available balance
        String goalContributionPattern = "Savings Goal Contribution: " + goal.getName();
        java.util.List<com.budgetwise.entity.Transaction> relatedTransactions = transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(t -> t.getDescription() != null && t.getDescription().equals(goalContributionPattern))
                .collect(java.util.stream.Collectors.toList());

        for (com.budgetwise.entity.Transaction transaction : relatedTransactions) {
            // Convert EXPENSE to INCOME (money returned)
            transaction.setType(com.budgetwise.entity.Transaction.TransactionType.INCOME);
            transaction.setDescription("Savings Goal Deleted: " + goal.getName() + " (Returned)");
            transactionRepository.save(transaction);
        }

        savingsGoalRepository.delete(goal);
    }

    private SavingsGoalDto mapToDto(SavingsGoal goal) {
        SavingsGoalDto dto = new SavingsGoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
        dto.setDeadline(goal.getDeadline());
        dto.setStatus(goal.getStatus());
        dto.setCreatedAt(goal.getCreatedAt());
        dto.setUpdatedAt(goal.getUpdatedAt());

        try {
            // Calculate progress percentage
            if (goal.getTargetAmount() != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentAmt = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
                BigDecimal progress = currentAmt
                        .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                dto.setProgressPercentage(progress);
            } else {
                dto.setProgressPercentage(BigDecimal.ZERO);
            }

            // Calculate required monthly savings
            if (goal.getDeadline() != null && goal.getStatus() == SavingsGoal.GoalStatus.ACTIVE) {
                long monthsRemaining = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getDeadline());
                if (monthsRemaining > 0) {
                    BigDecimal currentAmt = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
                    BigDecimal remaining = goal.getTargetAmount().subtract(currentAmt);
                    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal requiredMonthly = remaining
                                .divide(BigDecimal.valueOf(monthsRemaining), 2, RoundingMode.HALF_UP);
                        dto.setRequiredMonthlySavings(requiredMonthly);
                    } else {
                        dto.setRequiredMonthlySavings(BigDecimal.ZERO);
                    }
                } else {
                    dto.setRequiredMonthlySavings(BigDecimal.ZERO);
                }
            } else {
                dto.setRequiredMonthlySavings(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            // Log error but don't fail the entire response
            System.err
                    .println("Error calculating savings goal metrics for goal " + goal.getId() + ": " + e.getMessage());
            // Set safe defaults
            if (dto.getProgressPercentage() == null) {
                dto.setProgressPercentage(BigDecimal.ZERO);
            }
            if (dto.getRequiredMonthlySavings() == null) {
                dto.setRequiredMonthlySavings(BigDecimal.ZERO);
            }
        }

        return dto;
    }
}
