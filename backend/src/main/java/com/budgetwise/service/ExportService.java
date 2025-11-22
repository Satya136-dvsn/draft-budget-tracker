package com.budgetwise.service;

import com.budgetwise.dto.PredictionDto;
import com.budgetwise.entity.*;
import com.budgetwise.repository.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final CategoryRepository categoryRepository;
    private final PredictionService predictionService;

    // ========== CSV EXPORTS ==========

    public byte[] exportTransactionsCSV(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        writer.println("ID,Date,Type,Amount,Category ID,Description,Created At");

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
                    transaction.getCreatedAt().format(dateTimeFormatter));
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    public byte[] exportTransactionsExcel(Long userId, LocalDate startDate, LocalDate endDate) throws IOException {
        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = { "Date", "Type", "Amount", "Category", "Description" };
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int rowNum = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getTransactionDate().format(formatter));
                row.createCell(1).setCellValue(t.getType().toString());
                row.createCell(2).setCellValue(t.getAmount().doubleValue());

                String categoryName = "Uncategorized";
                if (t.getCategoryId() != null) {
                    java.util.Optional<Category> category = categoryRepository.findById(t.getCategoryId());
                    if (category.isPresent()) {
                        categoryName = category.get().getName();
                    }
                }
                row.createCell(3).setCellValue(categoryName);
                row.createCell(4).setCellValue(t.getDescription() != null ? t.getDescription() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportTransactionsPDF(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Transactions Report").setBold().setFontSize(20));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        if (startDate != null && endDate != null) {
            document.add(new Paragraph("Period: " + startDate + " to " + endDate));
        }
        document.add(new Paragraph(" "));

        Table table = new Table(5);
        // table width set to default

        Stream.of("Date", "Type", "Amount", "Category", "Description")
                .forEach(col -> table
                        .addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(col).setBold())));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Transaction t : transactions) {
            table.addCell(t.getTransactionDate().format(formatter));
            table.addCell(t.getType().toString());
            table.addCell(String.format("₹%.2f", t.getAmount()));

            String categoryName = "Uncategorized";
            if (t.getCategoryId() != null) {
                java.util.Optional<Category> category = categoryRepository.findById(t.getCategoryId());
                if (category.isPresent()) {
                    categoryName = category.get().getName();
                }
            }
            table.addCell(categoryName);
            table.addCell(t.getDescription() != null ? t.getDescription() : "");
        }

        document.add(table);
        document.close();
        return outputStream.toByteArray();
    }

    public byte[] exportAllDataCSV(Long userId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

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
                    t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : "");
        }

        writer.println();
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
                    b.getSpent());
        }

        writer.println();
        writer.println("=== SAVINGS GOALS ===");
        writer.println("ID,Name,Target Amount,Current Amount,Deadline,Status");
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);

        for (SavingsGoal g : goals) {
            writer.printf("%d,\"%s\",%.2f,%.2f,%s,%s%n",
                    g.getId(),
                    g.getName(),
                    g.getTargetAmount(),
                    g.getCurrentAmount(),
                    g.getDeadline() != null ? g.getDeadline().format(formatter) : "N/A",
                    g.getStatus());
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    public byte[] exportAllDataExcel(Long userId) throws IOException {
        return exportDashboardExcel(userId);
    }

    public byte[] exportAllDataPDF(Long userId) {
        return exportDashboardPDF(userId);
    }

    // ========== DASHBOARD EXPORTS ==========

    public byte[] exportDashboardExcel(Long userId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Sheet 1: Summary
            Sheet summarySheet = workbook.createSheet("Summary");
            createDashboardSummarySheet(summarySheet, userId, headerStyle);

            // Sheet 2: Monthly Trends
            Sheet trendsSheet = workbook.createSheet("Monthly Trends");
            createMonthlyTrendsSheet(trendsSheet, userId, headerStyle);

            // Sheet 3: Category Breakdown
            Sheet categorySheet = workbook.createSheet("Category Breakdown");
            createCategoryBreakdownSheet(categorySheet, transactionRepository.findByUserIdOrderByCreatedAtDesc(userId),
                    userId, headerStyle);

            // Sheet 4: Budgets
            Sheet budgetsSheet = workbook.createSheet("Budgets");
            createBudgetsSheet(budgetsSheet, userId);

            // Sheet 5: Savings Goals
            Sheet goalsSheet = workbook.createSheet("Savings Goals");
            createGoalsSheet(goalsSheet, userId);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportDashboardExcel(Long userId, Map<String, String> images) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Sheet summarySheet = workbook.createSheet("Summary");
            createDashboardSummarySheet(summarySheet, userId, headerStyle);

            if (images != null && !images.isEmpty()) {
                embedImagesInExcel(workbook, summarySheet, images, 10);
            }

            Sheet trendsSheet = workbook.createSheet("Monthly Trends");
            createMonthlyTrendsSheet(trendsSheet, userId, headerStyle);

            Sheet categorySheet = workbook.createSheet("Category Breakdown");
            createCategoryBreakdownSheet(categorySheet, transactionRepository.findByUserIdOrderByCreatedAtDesc(userId),
                    userId, headerStyle);

            Sheet budgetsSheet = workbook.createSheet("Budgets");
            createBudgetsSheet(budgetsSheet, userId);

            Sheet goalsSheet = workbook.createSheet("Savings Goals");
            createGoalsSheet(goalsSheet, userId);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportDashboardPDF(Long userId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Dashboard Report").setBold().setFontSize(20));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        addDashboardSummaryPDFSection(document, userId);
        addMonthlyTrendsPDFSection(document, userId);
        addCategoryBreakdownPDFSection(document, transactionRepository.findByUserIdOrderByCreatedAtDesc(userId),
                userId);
        addBudgetsPDFSection(document, userId);
        addGoalsPDFSection(document, userId);

        document.close();
        return outputStream.toByteArray();
    }

    public byte[] exportDashboardPDF(Long userId, Map<String, String> images) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Dashboard Report").setBold().setFontSize(20));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        addDashboardSummaryPDFSection(document, userId);

        if (images != null && !images.isEmpty()) {
            embedImagesInPDF(document, images);
        }

        addMonthlyTrendsPDFSection(document, userId);
        addCategoryBreakdownPDFSection(document, transactionRepository.findByUserIdOrderByCreatedAtDesc(userId),
                userId);
        addBudgetsPDFSection(document, userId);
        addGoalsPDFSection(document, userId);

        document.close();
        return outputStream.toByteArray();
    }

    // ========== ANALYTICS EXPORTS ==========

    public byte[] exportAnalyticsExcel(Long userId, String timeRange) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = calculateStartDate(timeRange, endDate);
            List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId,
                    startDate, endDate);

            Sheet summarySheet = workbook.createSheet("Summary Statistics");
            createAnalyticsSummarySheet(summarySheet, transactions, timeRange, headerStyle);

            Sheet categorySheet = workbook.createSheet("Category Breakdown");
            createCategoryBreakdownSheet(categorySheet, transactions, userId, headerStyle);

            Sheet predictionsSheet = workbook.createSheet("AI Predictions");
            createPredictionsSheet(predictionsSheet, userId, headerStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportAnalyticsExcel(Long userId, String timeRange, Map<String, String> images) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = calculateStartDate(timeRange, endDate);
            List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId,
                    startDate, endDate);

            Sheet summarySheet = workbook.createSheet("Summary Statistics");
            createAnalyticsSummarySheet(summarySheet, transactions, timeRange, headerStyle);

            if (images != null && !images.isEmpty()) {
                embedImagesInExcel(workbook, summarySheet, images, 10);
            }

            Sheet categorySheet = workbook.createSheet("Category Breakdown");
            createCategoryBreakdownSheet(categorySheet, transactions, userId, headerStyle);

            Sheet predictionsSheet = workbook.createSheet("AI Predictions");
            createPredictionsSheet(predictionsSheet, userId, headerStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportAnalyticsPDF(Long userId, String timeRange) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Analytics Report (" + timeRange + ")").setBold().setFontSize(20));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(timeRange, endDate);
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate,
                endDate);

        addAnalyticsSummaryPDFSection(document, transactions);
        addTrendAnalysisPDFSection(document, transactions);
        addCategoryBreakdownPDFSection(document, transactions, userId);
        addPredictionsPDFSection(document, userId);

        document.close();
        return outputStream.toByteArray();
    }

    public byte[] exportAnalyticsPDF(Long userId, String timeRange, Map<String, String> images) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Analytics Report (" + timeRange + ")").setBold().setFontSize(20));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(timeRange, endDate);
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate,
                endDate);

        addAnalyticsSummaryPDFSection(document, transactions);

        if (images != null && !images.isEmpty()) {
            embedImagesInPDF(document, images);
        }

        addTrendAnalysisPDFSection(document, transactions);
        addCategoryBreakdownPDFSection(document, transactions, userId);
        addPredictionsPDFSection(document, userId);

        document.close();
        return outputStream.toByteArray();
    }

    // ========== DASHBOARD HELPER METHODS ==========

    private void createDashboardSummarySheet(Sheet sheet, Long userId, CellStyle headerStyle) {
        List<Transaction> allTransactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        java.math.BigDecimal totalIncome = allTransactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalExpense = allTransactions.stream()
                .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Dashboard Summary");
        headerCell.setCellStyle(headerStyle);

        String[] labels = { "Total Income", "Total Expenses", "Net Savings" };
        java.math.BigDecimal[] values = { totalIncome, totalExpense, totalIncome.subtract(totalExpense) };

        for (int i = 0; i < labels.length; i++) {
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(labels[i]);
            row.createCell(1).setCellValue(String.format("₹%.2f", values[i]));
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createMonthlyTrendsSheet(Sheet sheet, Long userId, CellStyle headerStyle) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate,
                endDate);

        Row headerRow = sheet.createRow(0);
        String[] headers = { "Month", "Income", "Expenses", "Net Savings" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        java.util.Map<String, java.util.List<Transaction>> byMonth = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        java.util.List<String> sortedMonths = new java.util.ArrayList<>(byMonth.keySet());
        java.util.Collections.sort(sortedMonths);

        int rowNum = 1;
        for (String month : sortedMonths) {
            java.math.BigDecimal monthIncome = byMonth.get(month).stream()
                    .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            java.math.BigDecimal monthExpense = byMonth.get(month).stream()
                    .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(month);
            row.createCell(1).setCellValue(monthIncome.doubleValue());
            row.createCell(2).setCellValue(monthExpense.doubleValue());
            row.createCell(3).setCellValue(monthIncome.subtract(monthExpense).doubleValue());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createBudgetsSheet(Sheet sheet, Long userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);

        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "Category ID", "Amount", "Period", "Spent", "Remaining" };
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (Budget b : budgets) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(b.getId());
            row.createCell(1).setCellValue(b.getCategoryId());
            row.createCell(2).setCellValue(b.getAmount().doubleValue());
            row.createCell(3).setCellValue(b.getPeriod().toString());
            row.createCell(4).setCellValue(b.getSpent().doubleValue());
            row.createCell(5).setCellValue(b.getAmount().subtract(b.getSpent()).doubleValue());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createGoalsSheet(Sheet sheet, Long userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);

        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "Name", "Target", "Current", "Progress %", "Deadline", "Status" };
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int rowNum = 1;
        for (SavingsGoal g : goals) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(g.getId());
            row.createCell(1).setCellValue(g.getName());
            row.createCell(2).setCellValue(g.getTargetAmount().doubleValue());
            row.createCell(3).setCellValue(g.getCurrentAmount().doubleValue());

            double progress = g.getTargetAmount().doubleValue() > 0
                    ? (g.getCurrentAmount().doubleValue() / g.getTargetAmount().doubleValue()) * 100
                    : 0;
            row.createCell(4).setCellValue(progress);
            row.createCell(5).setCellValue(g.getDeadline() != null ? g.getDeadline().format(formatter) : "N/A");
            row.createCell(6).setCellValue(g.getStatus() != null ? g.getStatus().toString() : "ACTIVE");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addDashboardSummaryPDFSection(Document document, Long userId) {
        document.add(new Paragraph("Summary").setBold().setFontSize(14));

        List<Transaction> allTransactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        java.math.BigDecimal totalIncome = allTransactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalExpense = allTransactions.stream()
                .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal netSavings = totalIncome.subtract(totalExpense);

        Table table = new Table(2);
        // table width set to default

        table.addCell(new Paragraph("Total Income").setBold());
        table.addCell(String.format("₹%.2f", totalIncome));

        table.addCell(new Paragraph("Total Expenses").setBold());
        table.addCell(String.format("₹%.2f", totalExpense));

        table.addCell(new Paragraph("Net Savings").setBold());
        table.addCell(String.format("₹%.2f", netSavings));

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addMonthlyTrendsPDFSection(Document document, Long userId) {
        document.add(new Paragraph("Monthly Trends (Last 6 Months)").setBold().setFontSize(14));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate,
                endDate);

        Table table = new Table(4);
        // table width set to default

        Stream.of("Month", "Income", "Expenses", "Net Savings")
                .forEach(col -> table
                        .addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(col).setBold())));

        java.util.Map<String, java.util.List<Transaction>> byMonth = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        java.util.List<String> sortedMonths = new java.util.ArrayList<>(byMonth.keySet());
        java.util.Collections.sort(sortedMonths);

        for (String month : sortedMonths) {
            java.math.BigDecimal monthIncome = byMonth.get(month).stream()
                    .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            java.math.BigDecimal monthExpense = byMonth.get(month).stream()
                    .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            table.addCell(month);
            table.addCell(String.format("₹%.2f", monthIncome));
            table.addCell(String.format("₹%.2f", monthExpense));
            table.addCell(String.format("₹%.2f", monthIncome.subtract(monthExpense)));
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addBudgetsPDFSection(Document document, Long userId) {
        document.add(new Paragraph("Budgets").setBold().setFontSize(14));

        List<Budget> budgets = budgetRepository.findByUserId(userId);

        Table table = new Table(5);
        // table width set to default

        Stream.of("Category ID", "Amount", "Period", "Spent", "Remaining")
                .forEach(col -> table
                        .addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(col).setBold())));

        for (Budget b : budgets) {
            table.addCell(String.valueOf(b.getCategoryId()));
            table.addCell(String.format("₹%.2f", b.getAmount()));
            table.addCell(b.getPeriod().toString());
            table.addCell(String.format("₹%.2f", b.getSpent()));
            table.addCell(String.format("₹%.2f", b.getAmount().subtract(b.getSpent())));
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addGoalsPDFSection(Document document, Long userId) {
        document.add(new Paragraph("Savings Goals").setBold().setFontSize(14));

        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);

        Table table = new Table(5);
        // table width set to default

        Stream.of("Name", "Target", "Current", "Progress", "Deadline")
                .forEach(col -> table
                        .addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(col).setBold())));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (SavingsGoal g : goals) {
            table.addCell(g.getName());
            table.addCell(String.format("₹%.2f", g.getTargetAmount()));
            table.addCell(String.format("₹%.2f", g.getCurrentAmount()));

            double progress = g.getTargetAmount().doubleValue() > 0
                    ? (g.getCurrentAmount().doubleValue() / g.getTargetAmount().doubleValue()) * 100
                    : 0;
            table.addCell(String.format("%.1f%%", progress));
            table.addCell(g.getDeadline() != null ? g.getDeadline().format(formatter) : "N/A");
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    // ========== ANALYTICS HELPER METHODS ==========

    private void createAnalyticsSummarySheet(Sheet sheet, List<Transaction> transactions, String timeRange,
            CellStyle headerStyle) {
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Analytics Summary (" + timeRange + ")");
        titleCell.setCellStyle(headerStyle);

        java.math.BigDecimal totalIncome = transactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalExpense = transactions.stream()
                .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal netSavings = totalIncome.subtract(totalExpense);
        double savingsRate = totalIncome.compareTo(java.math.BigDecimal.ZERO) > 0
                ? netSavings.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100
                : 0.0;

        String[] labels = { "Total Income", "Total Expenses", "Net Savings", "Savings Rate" };
        String[] values = {
                String.format("₹%.2f", totalIncome),
                String.format("₹%.2f", totalExpense),
                String.format("₹%.2f", netSavings),
                String.format("%.1f%%", savingsRate)
        };

        for (int i = 0; i < labels.length; i++) {
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(labels[i]);
            row.createCell(1).setCellValue(values[i]);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createCategoryBreakdownSheet(Sheet sheet, List<Transaction> transactions, Long userId,
            CellStyle headerStyle) {
        List<Category> categories = categoryRepository.findAllByUserIdIncludingSystem(userId);
        java.util.Map<Long, String> categoryMap = categories.stream()
                .collect(java.util.stream.Collectors.toMap(Category::getId, Category::getName));

        java.util.Map<String, java.math.BigDecimal> categoryTotals = new java.util.HashMap<>();
        java.math.BigDecimal totalExpenses = java.math.BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (Transaction.TransactionType.EXPENSE.equals(t.getType())) {
                String categoryName = t.getCategoryId() != null
                        ? categoryMap.getOrDefault(t.getCategoryId(), "Uncategorized")
                        : "Uncategorized";
                java.math.BigDecimal amount = t.getAmount();
                categoryTotals.put(categoryName,
                        categoryTotals.getOrDefault(categoryName, java.math.BigDecimal.ZERO).add(amount));
                totalExpenses = totalExpenses.add(amount);
            }
        }

        Row headerRow = sheet.createRow(0);
        String[] headers = { "Category", "Amount", "Percentage" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        java.util.List<java.util.Map.Entry<String, java.math.BigDecimal>> sortedEntries = new java.util.ArrayList<>(
                categoryTotals.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (java.util.Map.Entry<String, java.math.BigDecimal> entry : sortedEntries) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());

            double percentage = totalExpenses.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalExpenses, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(java.math.BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            row.createCell(2).setCellValue(String.format("%.1f%%", percentage));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createPredictionsSheet(Sheet sheet, Long userId, CellStyle headerStyle) {
        List<PredictionDto> predictions = predictionService.predictNextMonthExpenses(userId);

        Row headerRow = sheet.createRow(0);
        String[] headers = { "Category", "Predicted Amount", "Historical Average", "Trend", "Confidence" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (PredictionDto p : predictions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getCategoryName());
            row.createCell(1).setCellValue(String.format("₹%.2f", p.getPredictedAmount()));
            row.createCell(2).setCellValue(String.format("₹%.2f", p.getHistoricalAverage()));
            row.createCell(3).setCellValue(p.getTrend());
            row.createCell(4).setCellValue(String.format("%.1f%%", p.getConfidenceScore() * 100));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addAnalyticsSummaryPDFSection(Document document, List<Transaction> transactions) {
        document.add(new Paragraph("Summary Statistics").setBold().setFontSize(14));

        java.math.BigDecimal totalIncome = transactions.stream()
                .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalExpense = transactions.stream()
                .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal netSavings = totalIncome.subtract(totalExpense);
        double savingsRate = totalIncome.compareTo(java.math.BigDecimal.ZERO) > 0
                ? netSavings.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100
                : 0.0;

        Table table = new Table(2);
        // table width set to default

        table.addCell(new Paragraph("Total Income").setBold());
        table.addCell(String.format("₹%.2f", totalIncome));

        table.addCell(new Paragraph("Total Expenses").setBold());
        table.addCell(String.format("₹%.2f", totalExpense));

        table.addCell(new Paragraph("Net Savings").setBold());
        table.addCell(String.format("₹%.2f", netSavings));

        table.addCell(new Paragraph("Savings Rate").setBold());
        table.addCell(String.format("%.1f%%", savingsRate));

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTrendAnalysisPDFSection(Document document, List<Transaction> transactions) {
        document.add(new Paragraph("Trend Analysis").setBold().setFontSize(14));

        java.util.Map<String, java.util.List<Transaction>> byMonth = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        if (byMonth.isEmpty()) {
            document.add(new Paragraph("No transaction data available for the selected period."));
            document.add(new Paragraph(" "));
            return;
        }

        java.util.List<String> sortedMonths = new java.util.ArrayList<>(byMonth.keySet());
        java.util.Collections.sort(sortedMonths);

        Table table = new Table(4);
        // table width set to default

        Stream.of("Month", "Income", "Expenses", "Net Savings")
                .forEach(col -> table
                        .addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(col).setBold())));

        for (String month : sortedMonths) {
            java.math.BigDecimal monthIncome = byMonth.get(month).stream()
                    .filter(t -> Transaction.TransactionType.INCOME.equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            java.math.BigDecimal monthExpense = byMonth.get(month).stream()
                    .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            table.addCell(month);
            table.addCell(String.format("₹%.2f", monthIncome));
            table.addCell(String.format("₹%.2f", monthExpense));
            table.addCell(String.format("₹%.2f", monthIncome.subtract(monthExpense)));
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addCategoryBreakdownPDFSection(Document document, List<Transaction> transactions, Long userId) {
        document.add(new Paragraph("Category Breakdown").setBold().setFontSize(14));

        List<Category> categories = categoryRepository.findAllByUserIdIncludingSystem(userId);
        java.util.Map<Long, String> categoryMap = categories.stream()
                .collect(java.util.stream.Collectors.toMap(Category::getId, Category::getName));

        java.util.Map<String, java.math.BigDecimal> categoryTotals = new java.util.HashMap<>();
        java.math.BigDecimal totalExpenses = java.math.BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (Transaction.TransactionType.EXPENSE.equals(t.getType())) {
                String categoryName = t.getCategoryId() != null
                        ? categoryMap.getOrDefault(t.getCategoryId(), "Uncategorized")
                        : "Uncategorized";
                java.math.BigDecimal amount = t.getAmount();
                categoryTotals.put(categoryName,
                        categoryTotals.getOrDefault(categoryName, java.math.BigDecimal.ZERO).add(amount));
                totalExpenses = totalExpenses.add(amount);
            }
        }

        Table table = new Table(3);
        // table width set to default

        Stream.of("Category", "Amount", "Percentage")
                .forEach(col -> table
                        .addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(col).setBold())));

        java.util.List<java.util.Map.Entry<String, java.math.BigDecimal>> sortedEntries = new java.util.ArrayList<>(
                categoryTotals.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (java.util.Map.Entry<String, java.math.BigDecimal> entry : sortedEntries) {
            table.addCell(entry.getKey());
            table.addCell(String.format("₹%.2f", entry.getValue()));

            double percentage = totalExpenses.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalExpenses, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(java.math.BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            table.addCell(String.format("%.1f%%", percentage));
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addPredictionsPDFSection(Document document, Long userId) {
        document.add(new Paragraph("AI Expense Predictions (Next Month)").setBold().setFontSize(14));

        List<PredictionDto> predictions = predictionService.predictNextMonthExpenses(userId);

        Table table = new Table(5);
        // table width set to default

        Stream.of("Category", "Predicted", "Avg", "Trend", "Conf.")
                .forEach(col -> table
                        .addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(col).setBold())));

        for (PredictionDto p : predictions) {
            table.addCell(p.getCategoryName());
            table.addCell(String.format("₹%.2f", p.getPredictedAmount()));
            table.addCell(String.format("₹%.2f", p.getHistoricalAverage()));
            table.addCell(p.getTrend());
            table.addCell(String.format("%.0f%%", p.getConfidenceScore() * 100));
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    // ========== BUDGETS ONLY EXPORTS ==========

    public byte[] exportBudgetsExcel(Long userId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Budgets");
            createBudgetsSheet(sheet, userId);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportBudgetsPDF(Long userId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Budgets Report").setBold().setFontSize(20));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        addBudgetsPDFSection(document, userId);

        document.close();
        return outputStream.toByteArray();
    }

    // ========== SAVINGS GOALS ONLY EXPORTS ==========

    public byte[] exportGoalsExcel(Long userId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Savings Goals");
            createGoalsSheet(sheet, userId);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportGoalsPDF(Long userId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Savings Goals Report").setBold().setFontSize(20));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        addGoalsPDFSection(document, userId);

        document.close();
        return outputStream.toByteArray();
    }

    // ========== UTILITY METHODS ==========

    private LocalDate calculateStartDate(String timeRange, LocalDate endDate) {
        if (timeRange == null)
            return endDate.minusMonths(3);

        switch (timeRange) {
            case "3M":
                return endDate.minusMonths(3);
            case "6M":
                return endDate.minusMonths(6);
            case "1Y":
                return endDate.minusYears(1);
            default:
                return endDate.minusMonths(3);
        }
    }

    private void embedImagesInPDF(Document document, Map<String, String> images) {
        for (Map.Entry<String, String> entry : images.entrySet()) {
            try {
                String base64Image = entry.getValue();
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Image img = new Image(ImageDataFactory.create(imageBytes));
                img.setAutoScale(true);
                document.add(new Paragraph(entry.getKey()).setBold());
                document.add(img);
                document.add(new Paragraph(" "));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void embedImagesInExcel(Workbook workbook, Sheet sheet, Map<String, String> images, int startRow) {
        int rowNum = startRow;
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        for (Map.Entry<String, String> entry : images.entrySet()) {
            try {
                String base64Image = entry.getValue();
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

                ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(rowNum);
                anchor.setCol2(10);
                anchor.setRow2(rowNum + 20);

                Picture pict = drawing.createPicture(anchor, pictureIdx);

                Row labelRow = sheet.getRow(rowNum - 1);
                if (labelRow == null)
                    labelRow = sheet.createRow(rowNum - 1);
                labelRow.createCell(0).setCellValue(entry.getKey());

                rowNum += 22;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

