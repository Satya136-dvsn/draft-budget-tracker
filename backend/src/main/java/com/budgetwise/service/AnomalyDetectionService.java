package com.budgetwise.service;

import com.budgetwise.dto.AnomalyDto;
import com.budgetwise.entity.Transaction;
import com.budgetwise.repository.TransactionRepository;
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
public class AnomalyDetectionService {

    private final TransactionRepository transactionRepository;

    public List<AnomalyDto> detectAnomalies(Long userId) {
        // Get last 3 months of transactions
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startDate, endDate);

        if (transactions.size() < 10) {
            return new ArrayList<>(); // Need sufficient data
        }

        // Group by category
        Map<Long, List<Transaction>> byCategory = transactions.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<AnomalyDto> anomalies = new ArrayList<>();

        byCategory.forEach((categoryId, categoryTransactions) -> {
            if (categoryTransactions.size() >= 5) { // Need at least 5 transactions
                anomalies.addAll(detectCategoryAnomalies(categoryId, categoryTransactions));
            }
        });

        // Sort by severity and z-score
        anomalies.sort((a, b) -> {
            int severityCompare = getSeverityOrder(b.getSeverity()) - getSeverityOrder(a.getSeverity());
            return severityCompare != 0 ? severityCompare : b.getZScore().compareTo(a.getZScore());
        });

        return anomalies;
    }

    private List<AnomalyDto> detectCategoryAnomalies(Long categoryId, List<Transaction> transactions) {
        List<AnomalyDto> anomalies = new ArrayList<>();

        // Calculate mean
        BigDecimal sum = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);

        // Calculate standard deviation
        double variance = transactions.stream()
                .mapToDouble(t -> {
                    double diff = t.getAmount().subtract(mean).doubleValue();
                    return diff * diff;
                })
                .average()
                .orElse(0.0);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance)).setScale(2, RoundingMode.HALF_UP);

        // Detect anomalies (z-score > 2)
        String categoryName = "Category " + categoryId;

        for (Transaction transaction : transactions) {
            if (stdDev.compareTo(BigDecimal.ZERO) > 0) {
                double zScore = transaction.getAmount().subtract(mean)
                        .divide(stdDev, 4, RoundingMode.HALF_UP)
                        .doubleValue();

                if (Math.abs(zScore) > 2.0) {
                    String severity = Math.abs(zScore) > 3.0 ? "HIGH" : "MEDIUM";
                    String reason = zScore > 0
                            ? String.format("Amount is %.1f standard deviations above average", zScore)
                            : String.format("Amount is %.1f standard deviations below average", Math.abs(zScore));

                    anomalies.add(AnomalyDto.builder()
                            .transactionId(transaction.getId())
                            .description(transaction.getDescription())
                            .amount(transaction.getAmount())
                            .categoryName(categoryName)
                            .date(transaction.getTransactionDate())
                            .categoryAverage(mean)
                            .standardDeviation(stdDev)
                            .zScore(zScore)
                            .severity(severity)
                            .reason(reason)
                            .build());
                }
            }
        }

        return anomalies;
    }

    private int getSeverityOrder(String severity) {
        return switch (severity) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    public void markAsExpected(Long transactionId, Long userId) {
        // Mark transaction as not anomalous
        transactionRepository.findById(transactionId).ifPresent(transaction -> {
            if (transaction.getUserId().equals(userId)) {
                transaction.setIsAnomaly(false);
                transactionRepository.save(transaction);
            }
        });
    }
}
