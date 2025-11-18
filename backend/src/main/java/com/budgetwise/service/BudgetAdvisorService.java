package com.budgetwise.service;

import com.budgetwise.dto.BudgetAdviceDto;
import com.budgetwise.entity.Transaction;
import com.budgetwise.entity.UserProfile;
import com.budgetwise.repository.TransactionRepository;
import com.budgetwise.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetAdvisorService {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;

    public List<BudgetAdviceDto> getPersonalizedAdvice(Long userId) {
        // Get current month transactions
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startOfMonth, endOfMonth);

        // Get user profile for income
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(null);

        BigDecimal monthlyIncome = (profile != null && profile.getMonthlyIncome() != null)
                ? profile.getMonthlyIncome()
                : BigDecimal.ZERO;

        if (monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return List.of(BudgetAdviceDto.builder()
                    .category("Profile Setup")
                    .recommendation("Set up your monthly income in your profile")
                    .priority("HIGH")
                    .actionItem("Go to Profile Settings and enter your monthly income")
                    .build());
        }

        // Calculate income and expenses
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Use actual income if available, otherwise use profile income
        BigDecimal effectiveIncome = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? totalIncome
                : monthlyIncome;

        // Group expenses by category
        Map<String, BigDecimal> categorySpending = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(
                        t -> "Category " + t.getCategoryId(), // In production, fetch category names
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        List<BudgetAdviceDto> advice = new ArrayList<>();

        // Analyze savings rate
        BigDecimal savingsRate = effectiveIncome.compareTo(BigDecimal.ZERO) > 0
                ? totalExpenses.subtract(effectiveIncome).abs()
                .divide(effectiveIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        if (savingsRate.compareTo(BigDecimal.valueOf(10)) < 0) {
            advice.add(BudgetAdviceDto.builder()
                    .category("Savings")
                    .recommendation("Your savings rate is below 10%. Try to save at least 10-20% of your income.")
                    .currentSpending(totalExpenses)
                    .recommendedSpending(effectiveIncome.multiply(BigDecimal.valueOf(0.8)))
                    .percentageOfIncome(savingsRate.doubleValue())
                    .priority("HIGH")
                    .actionItem("Review your expenses and identify areas to cut back")
                    .build());
        }

        // Analyze category spending
        categorySpending.forEach((category, amount) -> {
            Double percentage = effectiveIncome.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(effectiveIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                    : 0.0;

            // Flag categories exceeding 20% of income
            if (percentage > 20.0 && !category.equals("Rent")) {
                BigDecimal recommended = effectiveIncome.multiply(BigDecimal.valueOf(0.20));
                advice.add(BudgetAdviceDto.builder()
                        .category(category)
                        .recommendation(String.format("%s spending is %.1f%% of your income. Consider reducing to 20%% or less.",
                                category, percentage))
                        .currentSpending(amount)
                        .recommendedSpending(recommended)
                        .percentageOfIncome(percentage)
                        .priority("MEDIUM")
                        .actionItem(String.format("Reduce %s spending by %s",
                                category, amount.subtract(recommended).setScale(2, RoundingMode.HALF_UP)))
                        .build());
            }
        });

        // If no issues found, provide positive feedback
        if (advice.isEmpty()) {
            advice.add(BudgetAdviceDto.builder()
                    .category("Overall")
                    .recommendation("Great job! Your spending is well-balanced.")
                    .currentSpending(totalExpenses)
                    .percentageOfIncome(savingsRate.doubleValue())
                    .priority("LOW")
                    .actionItem("Keep up the good work and maintain your current habits")
                    .build());
        }

        // Sort by priority
        advice.sort((a, b) -> {
            int priorityOrder = getPriorityOrder(a.getPriority()) - getPriorityOrder(b.getPriority());
            return priorityOrder != 0 ? priorityOrder : b.getCurrentSpending().compareTo(a.getCurrentSpending());
        });

        // Return top 5 recommendations
        return advice.stream().limit(5).collect(Collectors.toList());
    }

    private int getPriorityOrder(String priority) {
        return switch (priority) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 4;
        };
    }
}
