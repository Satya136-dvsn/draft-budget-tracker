package com.budgetwise.service;

import com.budgetwise.dto.BudgetDto;
import com.budgetwise.entity.Budget;
import com.budgetwise.entity.Category;
import com.budgetwise.exception.ResourceNotFoundException;
import com.budgetwise.repository.BudgetRepository;
import com.budgetwise.repository.CategoryRepository;
import com.budgetwise.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public BudgetDto createBudget(BudgetDto dto, Long userId) {
        // Validate category if provided
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Check for overlapping budgets
        List<Budget> overlapping = budgetRepository.findOverlappingBudgets(
                userId, dto.getCategoryId(), dto.getStartDate(), dto.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("A budget already exists for this category and period");
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(dto.getCategoryId());
        budget.setAmount(dto.getAmount());
        budget.setPeriod(dto.getPeriod());
        budget.setStartDate(dto.getStartDate());
        budget.setEndDate(dto.getEndDate());
        budget.setAlertThreshold(dto.getAlertThreshold() != null ? dto.getAlertThreshold() : new BigDecimal("80.00"));
        budget.setSpent(BigDecimal.ZERO);

        Budget saved = budgetRepository.save(budget);

        // Calculate initial spent amount
        updateBudgetProgress(userId, saved.getCategoryId());

        return mapToDto(budgetRepository.findById(saved.getId()).orElseThrow());
    }

    public List<BudgetDto> getAllBudgets(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BudgetDto> getActiveBudgets(Long userId) {
        return budgetRepository.findActiveBudgets(userId, LocalDate.now()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BudgetDto getBudgetById(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        return mapToDto(budget);
    }

    @Transactional
    public BudgetDto updateBudget(Long id, BudgetDto dto, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        // Validate category if changed
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(budget.getCategoryId())) {
            categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        budget.setAmount(dto.getAmount());
        budget.setCategoryId(dto.getCategoryId());
        budget.setPeriod(dto.getPeriod());
        budget.setStartDate(dto.getStartDate());
        budget.setEndDate(dto.getEndDate());
        if (dto.getAlertThreshold() != null) {
            budget.setAlertThreshold(dto.getAlertThreshold());
        }

        Budget updated = budgetRepository.save(budget);
        updateBudgetProgress(userId, updated.getCategoryId());

        return mapToDto(budgetRepository.findById(updated.getId()).orElseThrow());
    }

    @Transactional
    public void deleteBudget(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }

    @Transactional
    public BudgetDto addContribution(Long id, com.budgetwise.dto.ContributionRequest request, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        // Create expense transaction for the contribution
        com.budgetwise.dto.TransactionDto transactionDto = new com.budgetwise.dto.TransactionDto();
        transactionDto.setAmount(request.getAmount());
        transactionDto.setCategoryId(budget.getCategoryId());
        transactionDto
                .setDescription(request.getDescription() != null ? request.getDescription() : "Budget Contribution");
        transactionDto.setTransactionDate(
                request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        transactionDto.setType(com.budgetwise.entity.Transaction.TransactionType.EXPENSE);

        // We need to use a circular dependency safe way or just save it directly if
        // service is not available
        // Since we are in BudgetService, we can't easily inject TransactionService if
        // it injects BudgetService (circular)
        // But TransactionService injects BudgetService. So we should use
        // TransactionRepository directly or break the cycle.
        // However, TransactionService logic includes "Update budget progress".
        // If we create transaction manually here, we need to ensure budget progress is
        // updated.
        // Actually, the requirement is "adding the amount in budgets... should reflect
        // in transactions".
        // So we should create a transaction.

        com.budgetwise.entity.Transaction transaction = new com.budgetwise.entity.Transaction();
        transaction.setUserId(userId);
        transaction.setType(com.budgetwise.entity.Transaction.TransactionType.EXPENSE);
        transaction.setAmount(request.getAmount());
        transaction.setCategoryId(budget.getCategoryId());
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Budget Contribution");
        transaction.setTransactionDate(
                request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        transaction.setIsAnomaly(false);

        transactionRepository.save(transaction);

        // Update budget progress
        updateBudgetProgress(userId, budget.getCategoryId());

        return mapToDto(budgetRepository.findById(budget.getId()).orElseThrow());
    }

    @Transactional
    public void updateBudgetProgress(Long userId, Long categoryId) {
        if (categoryId == null)
            return;

        List<Budget> budgets = budgetRepository.findActiveBudgets(userId, LocalDate.now()).stream()
                .filter(b -> categoryId.equals(b.getCategoryId()))
                .collect(Collectors.toList());

        for (Budget budget : budgets) {
            BigDecimal spent = transactionRepository.calculateSpentForBudget(
                    userId, categoryId, budget.getStartDate(), budget.getEndDate());
            budget.setSpent(spent != null ? spent : BigDecimal.ZERO);
            budgetRepository.save(budget);
        }
    }

    private BudgetDto mapToDto(Budget budget) {
        BudgetDto dto = new BudgetDto();
        dto.setId(budget.getId());
        dto.setAmount(budget.getAmount());
        dto.setCategoryId(budget.getCategoryId());

        if (budget.getCategoryId() != null) {
            dto.setCategoryName(categoryRepository.findById(budget.getCategoryId())
                    .map(Category::getName)
                    .orElse(null));
        }

        dto.setPeriod(budget.getPeriod());
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());
        dto.setAlertThreshold(budget.getAlertThreshold());
        dto.setSpent(budget.getSpent());

        // Calculate remaining and progress
        BigDecimal remaining = budget.getAmount().subtract(budget.getSpent());
        dto.setRemaining(remaining);

        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = budget.getSpent()
                    .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setProgressPercentage(progress);
        } else {
            dto.setProgressPercentage(BigDecimal.ZERO);
        }

        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());

        return dto;
    }
}
