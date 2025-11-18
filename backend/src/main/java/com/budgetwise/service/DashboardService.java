package com.budgetwise.service;

import com.budgetwise.dto.*;
import com.budgetwise.entity.Transaction;
import com.budgetwise.repository.BudgetRepository;
import com.budgetwise.repository.SavingsGoalRepository;
import com.budgetwise.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    @Cacheable(value = "dashboardSummary", key = "#userId")
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startOfMonth, endOfMonth);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        Double savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? balance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        Integer budgetCount = budgetRepository.countByUserId(userId);
        Integer goalCount = savingsGoalRepository.countByUserId(userId);

        return DashboardSummaryDto.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .balance(balance)
                .savingsRate(savingsRate)
                .transactionCount(transactions.size())
                .budgetCount(budgetCount)
                .goalCount(goalCount)
                .build();
    }

    @Cacheable(value = "monthlyTrends", key = "#userId + '_' + #months")
    public List<MonthlyTrendDto> getMonthlyTrends(Long userId, Integer months) {
        if (months == null || months <= 0) {
            months = 6;
        }

        List<MonthlyTrendDto> trends = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(endDate.minusMonths(i));
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();

            List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                    userId, startOfMonth, endOfMonth);

            BigDecimal income = transactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expenses = transactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.add(MonthlyTrendDto.builder()
                    .month(startOfMonth.format(formatter))
                    .income(income)
                    .expenses(expenses)
                    .netSavings(income.subtract(expenses))
                    .build());
        }

        return trends;
    }

    @Cacheable(value = "categoryBreakdown", key = "#userId")
    public List<CategoryBreakdownDto> getCategoryBreakdown(Long userId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startOfMonth, endOfMonth);

        // Filter only expenses for category breakdown
        List<Transaction> expenses = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.toList());

        BigDecimal totalExpenses = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by category
        Map<Long, List<Transaction>> groupedByCategory = expenses.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<CategoryBreakdownDto> breakdown = new ArrayList<>();

        groupedByCategory.forEach((categoryId, categoryTransactions) -> {
            BigDecimal categoryAmount = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? categoryAmount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                    : 0.0;

            // Fetch category name (in production, this should be optimized with a join or cache)
            String categoryName = "Category " + categoryId;

            breakdown.add(CategoryBreakdownDto.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .amount(categoryAmount)
                    .percentage(percentage)
                    .transactionCount(categoryTransactions.size())
                    .build());
        });

        // Sort by amount descending
        breakdown.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        return breakdown;
    }

    public List<TransactionDto> getRecentTransactions(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<Transaction> transactions = transactionRepository.findTop10ByUserIdOrderByTransactionDateDescCreatedAtDesc(userId);

        return transactions.stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TransactionDto convertToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .categoryId(transaction.getCategoryId())
                .categoryName("Category") // In production, fetch from category service
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
