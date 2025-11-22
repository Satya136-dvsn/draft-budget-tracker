package com.budgetwise.service;

import com.budgetwise.dto.ReportDto;
import com.budgetwise.entity.ScheduledReport;
import com.budgetwise.entity.Transaction;
import com.budgetwise.entity.Budget;
import com.budgetwise.entity.SavingsGoal;
import com.budgetwise.entity.Category;
import com.budgetwise.repository.BudgetRepository;
import com.budgetwise.repository.CategoryRepository;
import com.budgetwise.repository.SavingsGoalRepository;
import com.budgetwise.repository.ScheduledReportRepository;
import com.budgetwise.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final ScheduledReportRepository scheduledReportRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public byte[] generateTemplateReport(Long userId, int templateId) {
        StringBuilder report = new StringBuilder();
        appendHeader(report, getTemplateName(templateId));

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now;

        switch (templateId) {
            case 1: // Monthly Summary
                generateMonthlySummary(report, userId, startDate, endDate);
                break;
            case 2: // Tax Report
                generateTaxReport(report, userId, now.minusYears(1).withDayOfYear(1),
                        now.minusYears(1).withDayOfYear(now.minusYears(1).lengthOfYear()));
                break;
            case 3: // Expense Analysis
                generateExpenseAnalysis(report, userId, startDate, endDate);
                break;
            case 4: // Investment Performance
                generateInvestmentReport(report, userId);
                break;
            default:
                report.append("Unknown template ID: ").append(templateId);
        }

        appendFooter(report);
        return report.toString().getBytes();
    }

    @Transactional(readOnly = true)
    public byte[] generateCustomReport(Long userId, ReportDto.CustomReportConfig config) {
        StringBuilder report = new StringBuilder();
        appendHeader(report, config.getName());

        report.append("Date Range: ").append(config.getDateRange()).append("\n\n");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(config.getDateRange(), endDate);

        if (config.getMetrics().getOrDefault("income", false)) {
            generateIncomeSection(report, userId, startDate, endDate);
        }
        if (config.getMetrics().getOrDefault("expenses", false)) {
            generateExpenseSection(report, userId, startDate, endDate);
        }
        if (config.getMetrics().getOrDefault("savings", false)) {
            generateSavingsSection(report, userId, startDate, endDate);
        }
        if (config.getMetrics().getOrDefault("budgets", false)) {
            generateBudgetSection(report, userId);
        }
        if (config.getMetrics().getOrDefault("goals", false)) {
            generateGoalsSection(report, userId);
        }

        appendFooter(report);
        return report.toString().getBytes();
    }

    // --- Helper Methods for Report Generation ---

    private void generateMonthlySummary(StringBuilder report, Long userId, LocalDate start, LocalDate end) {
        report.append("MONTHLY SUMMARY (").append(start.getMonth()).append(" ").append(start.getYear()).append(")\n");
        report.append("-------------------------------------------\n");
        generateIncomeSection(report, userId, start, end);
        generateExpenseSection(report, userId, start, end);
        generateSavingsSection(report, userId, start, end);
    }

    private void generateTaxReport(StringBuilder report, Long userId, LocalDate start, LocalDate end) {
        report.append("TAX REPORT (").append(start.getYear()).append(")\n");
        report.append("Period: ").append(start).append(" to ").append(end).append("\n");
        report.append("-------------------------------------------\n");

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start,
                end);
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.append("Total Taxable Income: ").append(formatCurrency(totalIncome)).append("\n\n");
        report.append("Deductible Expenses:\n");
        report.append("  (No tax-deductible categories configured)\n");
    }

    private void generateExpenseAnalysis(StringBuilder report, Long userId, LocalDate start, LocalDate end) {
        report.append("EXPENSE ANALYSIS\n");
        report.append("-------------------------------------------\n");
        generateExpenseSection(report, userId, start, end);
    }

    private void generateInvestmentReport(StringBuilder report, Long userId) {
        report.append("INVESTMENT PERFORMANCE\n");
        report.append("-------------------------------------------\n");
        report.append("Investment tracking is not yet fully implemented.\n");
        report.append("Please connect your investment accounts.\n");
    }

    private void generateIncomeSection(StringBuilder report, Long userId, LocalDate start, LocalDate end) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start,
                end);
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.append("INCOME SUMMARY\n");
        report.append("Total Income:           ").append(formatCurrency(totalIncome)).append("\n");

        Map<String, BigDecimal> byCategory = transactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> getCategoryName(t.getCategoryId()),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        byCategory.forEach((cat, amount) -> report.append("  - ").append(String.format("%-20s", cat))
                .append(formatCurrency(amount)).append("\n"));
        report.append("\n");
    }

    private void generateExpenseSection(StringBuilder report, Long userId, LocalDate start, LocalDate end) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start,
                end);
        BigDecimal totalExpense = transactions.stream()
                .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.append("EXPENSE SUMMARY\n");
        report.append("Total Expenses:         ").append(formatCurrency(totalExpense.abs())).append("\n");

        Map<String, BigDecimal> byCategory = transactions.stream()
                .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> getCategoryName(t.getCategoryId()),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        byCategory.forEach((cat, amount) -> report.append("  - ").append(String.format("%-20s", cat))
                .append(formatCurrency(amount.abs())).append("\n"));
        report.append("\n");
    }

    private void generateSavingsSection(StringBuilder report, Long userId, LocalDate start, LocalDate end) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start,
                end);
        BigDecimal income = transactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = transactions.stream()
                .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        BigDecimal savings = income.subtract(expense);
        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                ? savings.doubleValue() / income.doubleValue() * 100
                : 0;

        report.append("NET SAVINGS\n");
        report.append("Net Savings:            ").append(formatCurrency(savings)).append("\n");
        report.append("Savings Rate:           ").append(String.format("%.2f%%", savingsRate)).append("\n\n");
    }

    private void generateBudgetSection(StringBuilder report, Long userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        report.append("BUDGET PERFORMANCE\n");
        for (Budget b : budgets) {
            report.append("  - ").append(String.format("%-20s", getCategoryName(b.getCategoryId())))
                    .append("Limit: ").append(formatCurrency(b.getAmount()))
                    .append(" | Spent: ").append(formatCurrency(b.getSpent()))
                    .append("\n");
        }
        report.append("\n");
    }

    private void generateGoalsSection(StringBuilder report, Long userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);
        report.append("GOAL PROGRESS\n");
        for (SavingsGoal g : goals) {
            report.append("  - ").append(String.format("%-20s", g.getName()))
                    .append("Target: ").append(formatCurrency(g.getTargetAmount()))
                    .append(" | Current: ").append(formatCurrency(g.getCurrentAmount()))
                    .append("\n");
        }
        report.append("\n");
    }

    private String getCategoryName(Long categoryId) {
        if (categoryId == null)
            return "Uncategorized";
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Uncategorized");
    }

    private void appendHeader(StringBuilder report, String title) {
        report.append("===========================================\n");
        report.append("BUDGETWISE FINANCIAL REPORT\n");
        report.append(title).append("\n");
        report.append("===========================================\n\n");
        report.append("Generated on: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        report.append("-------------------------------------------\n");
    }

    private void appendFooter(StringBuilder report) {
        report.append("-------------------------------------------\n");
        report.append("Report generated by BudgetWise\n");
        report.append("© 2025 Budget Wise - Smart Finance Management\n");
        report.append("===========================================\n");
    }

    private String getTemplateName(int id) {
        switch (id) {
            case 1:
                return "Monthly Summary";
            case 2:
                return "Tax Report";
            case 3:
                return "Expense Analysis";
            case 4:
                return "Investment Performance";
            default:
                return "Report";
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "₹0.00";
        return "₹" + String.format("%,.2f", amount);
    }

    private LocalDate calculateStartDate(String range, LocalDate endDate) {
        if (range == null)
            return endDate.withDayOfMonth(1);
        switch (range) {
            case "last_month":
                return endDate.minusMonths(1).withDayOfMonth(1);
            case "last_quarter":
                return endDate.minusMonths(3);
            case "last_year":
                return endDate.minusYears(1);
            default:
                return endDate.withDayOfMonth(1); // Default to this month
        }
    }

    // --- Scheduled Reports CRUD ---

    public List<ReportDto.ScheduledReportDto> getScheduledReports(Long userId) {
        return scheduledReportRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportDto.ScheduledReportDto createScheduledReport(Long userId, ReportDto.ScheduledReportDto dto) {
        ScheduledReport report = ScheduledReport.builder()
                .userId(userId)
                .reportName(dto.getName())
                .reportType(dto.getReportType() != null ? dto.getReportType() : "TEMPLATE")
                .frequency(dto.getFrequency())
                .recipients(dto.getRecipients())
                .active(true)
                .nextRun(calculateNextRun(dto.getFrequency()))
                .build();

        ScheduledReport saved = scheduledReportRepository.save(report);
        return mapToDto(saved);
    }

    @Transactional
    public ReportDto.ScheduledReportDto updateScheduledReport(Long userId, Long reportId,
            ReportDto.ScheduledReportDto dto) {
        ScheduledReport report = scheduledReportRepository.findById(reportId)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setReportName(dto.getName());
        report.setFrequency(dto.getFrequency());
        report.setRecipients(dto.getRecipients());
        if (dto.getStatus() != null) {
            report.setActive("Active".equalsIgnoreCase(dto.getStatus()));
        }

        ScheduledReport updated = scheduledReportRepository.save(report);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteScheduledReport(Long userId, Long reportId) {
        ScheduledReport report = scheduledReportRepository.findById(reportId)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Report not found"));
        scheduledReportRepository.delete(report);
    }

    private ReportDto.ScheduledReportDto mapToDto(ScheduledReport entity) {
        return ReportDto.ScheduledReportDto.builder()
                .id(entity.getId())
                .name(entity.getReportName())
                .frequency(entity.getFrequency())
                .nextRun(entity.getNextRun() != null ? entity.getNextRun().toLocalDate().toString() : "N/A")
                .recipients(entity.getRecipients())
                .status(entity.isActive() ? "Active" : "Paused")
                .reportType(entity.getReportType())
                .build();
    }

    private LocalDateTime calculateNextRun(String frequency) {
        LocalDateTime now = LocalDateTime.now();
        if ("WEEKLY".equalsIgnoreCase(frequency))
            return now.plusWeeks(1);
        if ("MONTHLY".equalsIgnoreCase(frequency))
            return now.plusMonths(1);
        if ("QUARTERLY".equalsIgnoreCase(frequency))
            return now.plusMonths(3);
        return now.plusDays(1);
    }
}
