package com.budgetwise.service;

import com.budgetwise.entity.Budget;
import com.budgetwise.entity.SavingsGoal;
import com.budgetwise.entity.Transaction;
import com.budgetwise.repository.BudgetRepository;
import com.budgetwise.repository.SavingsGoalRepository;
import com.budgetwise.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public byte[] exportTransactionsCSV(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                    userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("ID,Date,Type,Amount,Category ID,Description,Created At");

        // CSV Data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Transaction transaction : transactions) {
            writer.printf("%d,%s,%s,%.2f,%d,\"%s\",%s%n",
                    transaction.getId(),
                    transaction.getTransactionDate().format(formatter),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getCategoryId(),
                    transaction.getDescription() != null ? transaction.getDescription().replace("\"", "\"\"") : "",
                    transaction.getCreatedAt().format(dateTimeFormatter)
            );
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    public byte[] exportAllDataCSV(Long userId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // Export Transactions
        writer.println("=== TRANSACTIONS ===");
        writer.println("ID,Date,Type,Amount,Category ID,Description");
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Transaction t : transactions) {
            writer.printf("%d,%s,%s,%.2f,%d,\"%s\"%n",
                    t.getId(),
                    t.getTransactionDate().format(formatter),
                    t.getType(),
                    t.getAmount(),
                    t.getCategoryId(),
                    t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : ""
            );
        }

        writer.println();

        // Export Budgets
        writer.println("=== BUDGETS ===");
        writer.println("ID,Category ID,Amount,Period,Start Date,End Date,Spent");
        List<Budget> budgets = budgetRepository.findByUserId(userId);

        for (Budget b : budgets) {
            writer.printf("%d,%d,%.2f,%s,%s,%s,%.2f%n",
                    b.getId(),
                    b.getCategoryId(),
                    b.getAmount(),
                    b.getPeriod(),
                    b.getStartDate().format(formatter),
                    b.getEndDate().format(formatter),
                    b.getSpent()
            );
        }

        writer.println();

        // Export Savings Goals
        writer.println("=== SAVINGS GOALS ===");
        writer.println("ID,Name,Target Amount,Current Amount,Deadline,Status");
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);

        for (SavingsGoal g : goals) {
            writer.printf("%d,\"%s\",%.2f,%.2f,%s,%s%n",
                    g.getId(),
                    g.getName().replace("\"", "\"\""),
                    g.getTargetAmount(),
                    g.getCurrentAmount(),
                    g.getDeadline() != null ? g.getDeadline().format(formatter) : "N/A",
                    g.getStatus()
            );
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }
}
