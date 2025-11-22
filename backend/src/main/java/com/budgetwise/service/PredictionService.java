package com.budgetwise.service;

import com.budgetwise.dto.PredictionDto;
import com.budgetwise.entity.Transaction;
import com.budgetwise.entity.Category;
import com.budgetwise.repository.TransactionRepository;
import com.budgetwise.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.cache.annotation.Cacheable;
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
public class PredictionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

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
            // Changed from 3 to 1 to show all categories with any spending
            if (categoryTransactions.size() >= 1) {
                try {
                    PredictionDto prediction = predictForCategory(categoryId, categoryTransactions);
                    if (prediction != null) {
                        predictions.add(prediction);
                    }
                } catch (Exception e) {
                    // Skip this category if prediction fails
                    System.err.println("Failed to predict for category " + categoryId + ": " + e.getMessage());
                }
            }
        });

        // Add total prediction summary
        if (!predictions.isEmpty()) {
            PredictionDto totalPrediction = calculateTotalPrediction(predictions);
            predictions.add(0, totalPrediction); // Add at beginning
        }

        return predictions;
    }

    private PredictionDto calculateTotalPrediction(List<PredictionDto> categoryPredictions) {
        BigDecimal totalPredicted = categoryPredictions.stream()
                .map(PredictionDto::getPredictedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHistorical = categoryPredictions.stream()
                .map(PredictionDto::getHistoricalAverage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average confidence across all categories
        Double avgConfidence = categoryPredictions.stream()
                .mapToDouble(PredictionDto::getConfidenceScore)
                .average()
                .orElse(0.5);

        // Determine overall trend based on total predicted vs historical
        String overallTrend;
        if (totalPredicted.compareTo(totalHistorical.multiply(BigDecimal.valueOf(1.05))) > 0) {
            overallTrend = "INCREASING";
        } else if (totalPredicted.compareTo(totalHistorical.multiply(BigDecimal.valueOf(0.95))) < 0) {
            overallTrend = "DECREASING";
        } else {
            overallTrend = "STABLE";
        }

        return PredictionDto.builder()
                .categoryId(null)
                .categoryName("Total Monthly Expenses")
                .predictedAmount(totalPredicted)
                .historicalAverage(totalHistorical)
                .confidenceScore(avgConfidence)
                .trend(overallTrend)
                .build();
    }

    private PredictionDto predictForCategory(Long categoryId, List<Transaction> transactions) {
        // Fetch actual category name from database
        String categoryName = categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Category " + categoryId);

        // Calculate monthly averages
        Map<String, BigDecimal> monthlyTotals = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().getYear() + "-" + t.getTransactionDate().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        // Prepare data for regression
        SimpleRegression regression = new SimpleRegression();
        List<BigDecimal> amounts = new ArrayList<>(monthlyTotals.values());

        for (int i = 0; i < amounts.size(); i++) {
            regression.addData(i, amounts.get(i).doubleValue());
        }

        // Calculate historical average (always needed as fallback)
        BigDecimal historicalAverage = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);

        // Predict next month (next index)
        double predictedValue = regression.predict(amounts.size());
        BigDecimal predictedAmount;
        Double confidenceScore;

        // Handle NaN and invalid values from regression
        if (Double.isNaN(predictedValue) || Double.isInfinite(predictedValue) || predictedValue < 0) {
            // Fallback to historical average with trend adjustment
            predictedAmount = historicalAverage;

            // Apply trend adjustment if we have enough data
            if (amounts.size() >= 2) {
                String trend = determineTrend(amounts);
                if ("INCREASING".equals(trend)) {
                    // Increase by 5% for increasing trend
                    predictedAmount = historicalAverage.multiply(BigDecimal.valueOf(1.05))
                            .setScale(2, RoundingMode.HALF_UP);
                } else if ("DECREASING".equals(trend)) {
                    // Decrease by 5% for decreasing trend
                    predictedAmount = historicalAverage.multiply(BigDecimal.valueOf(0.95))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            // Lower confidence when using fallback (0-1 range, frontend will multiply by
            // 100)
            confidenceScore = 0.40 + (amounts.size() * 0.05); // 40-70% when displayed
            confidenceScore = Math.min(0.70, confidenceScore);
        } else {
            // Use regression prediction
            predictedAmount = BigDecimal.valueOf(Math.max(0, predictedValue))
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate confidence score based on R-squared (0-1 range)
            double rSquared = regression.getRSquare();
            if (Double.isNaN(rSquared) || Double.isInfinite(rSquared) || rSquared < 0) {
                confidenceScore = 0.50; // 50% when displayed
            } else {
                // R-squared is already 0-1, frontend will multiply by 100
                confidenceScore = Math.max(0, Math.min(1.0, rSquared));
            }
        }

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
