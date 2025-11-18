package com.budgetwise.service;

import com.budgetwise.dto.PredictionDto;
import com.budgetwise.entity.Transaction;
import com.budgetwise.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final TransactionRepository transactionRepository;

    @Cacheable(value = "predictions", key = "#userId")
    public List<PredictionDto> predictNextMonthExpenses(Long userId) {
        // Get last 6 months of data
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startDate, endDate);

        // Filter expenses only
        List<Transaction> expenses = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.toList());

        if (expenses.isEmpty()) {
            return new ArrayList<>();
        }

        // Group by category
        Map<Long, List<Transaction>> byCategory = expenses.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<PredictionDto> predictions = new ArrayList<>();

        byCategory.forEach((categoryId, categoryTransactions) -> {
            if (categoryTransactions.size() >= 3) { // Need at least 3 data points
                PredictionDto prediction = predictForCategory(categoryId, categoryTransactions);
                predictions.add(prediction);
            }
        });

        return predictions;
    }

    private PredictionDto predictForCategory(Long categoryId, List<Transaction> transactions) {
        String categoryName = "Category " + categoryId; // In production, fetch from category service

        // Calculate monthly averages
        Map<String, BigDecimal> monthlyTotals = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().getYear() + "-" + t.getTransactionDate().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // Prepare data for regression
        SimpleRegression regression = new SimpleRegression();
        List<BigDecimal> amounts = new ArrayList<>(monthlyTotals.values());

        for (int i = 0; i < amounts.size(); i++) {
            regression.addData(i, amounts.get(i).doubleValue());
        }

        // Predict next month (next index)
        double predictedValue = regression.predict(amounts.size());
        BigDecimal predictedAmount = BigDecimal.valueOf(Math.max(0, predictedValue))
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate historical average
        BigDecimal historicalAverage = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);

        // Calculate confidence score based on R-squared
        double rSquared = regression.getRSquare();
        Double confidenceScore = Math.max(0, Math.min(100, rSquared * 100));

        // Determine trend
        String trend = determineTrend(amounts);

        return PredictionDto.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .predictedAmount(predictedAmount)
                .historicalAverage(historicalAverage)
                .confidenceScore(confidenceScore)
                .trend(trend)
                .build();
    }

    private String determineTrend(List<BigDecimal> amounts) {
        if (amounts.size() < 2) {
            return "STABLE";
        }

        BigDecimal first = amounts.get(0);
        BigDecimal last = amounts.get(amounts.size() - 1);

        BigDecimal change = last.subtract(first);
        BigDecimal percentChange = change.divide(first, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (percentChange.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "INCREASING";
        } else if (percentChange.compareTo(BigDecimal.valueOf(-10)) < 0) {
            return "DECREASING";
        } else {
            return "STABLE";
        }
    }
}
