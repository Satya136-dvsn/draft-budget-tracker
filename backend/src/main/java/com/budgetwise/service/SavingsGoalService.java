package com.budgetwise.service;

import com.budgetwise.dto.ContributionRequest;
import com.budgetwise.dto.SavingsGoalDto;
import com.budgetwise.entity.SavingsGoal;
import com.budgetwise.exception.ResourceNotFoundException;
import com.budgetwise.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
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
        return mapToDto(updated);
    }
    
    @Transactional
    public void deleteGoal(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        savingsGoalRepository.delete(goal);
    }
    
    private SavingsGoalDto mapToDto(SavingsGoal goal) {
        SavingsGoalDto dto = new SavingsGoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setDeadline(goal.getDeadline());
        dto.setStatus(goal.getStatus());
        dto.setCreatedAt(goal.getCreatedAt());
        dto.setUpdatedAt(goal.getUpdatedAt());
        
        // Calculate progress percentage
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = goal.getCurrentAmount()
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
                BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
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
        }
        
        return dto;
    }
}
