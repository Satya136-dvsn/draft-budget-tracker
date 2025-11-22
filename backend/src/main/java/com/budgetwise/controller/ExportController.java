package com.budgetwise.controller;

import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/transactions")
    public ResponseEntity<byte[]> exportTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) throws java.io.IOException {

        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format.toLowerCase()) {
            case "excel":
                data = exportService.exportTransactionsExcel(userPrincipal.getId(), startDate, endDate);
                filename = "transactions.xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                data = exportService.exportTransactionsPDF(userPrincipal.getId(), startDate, endDate);
                filename = "transactions.pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "csv":
            default:
                data = exportService.exportTransactionsCSV(userPrincipal.getId(), startDate, endDate);
                filename = "transactions.csv";
                mediaType = MediaType.parseMediaType("text/csv");
                break;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/all-data")
    public ResponseEntity<byte[]> exportAllData(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "csv") String format) throws java.io.IOException {

        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format.toLowerCase()) {
            case "excel":
                data = exportService.exportAllDataExcel(userPrincipal.getId());
                filename = "budgetwise-data.xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                data = exportService.exportAllDataPDF(userPrincipal.getId());
                filename = "budgetwise-data.pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "csv":
            default:
                data = exportService.exportAllDataCSV(userPrincipal.getId());
                filename = "budgetwise-data.csv";
                mediaType = MediaType.parseMediaType("text/csv");
                break;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<byte[]> exportDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "csv") String format) throws java.io.IOException {

        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format.toLowerCase()) {
            case "excel":
                data = exportService.exportDashboardExcel(userPrincipal.getId());
                filename = "dashboard.xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                data = exportService.exportDashboardPDF(userPrincipal.getId());
                filename = "dashboard.pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            default:
                throw new IllegalArgumentException("Dashboard export only supports Excel and PDF formats");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/budgets")
    public ResponseEntity<byte[]> exportBudgets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "excel") String format) throws java.io.IOException {

        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format.toLowerCase()) {
            case "excel":
                data = exportService.exportBudgetsExcel(userPrincipal.getId());
                filename = "budgets.xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                data = exportService.exportBudgetsPDF(userPrincipal.getId());
                filename = "budgets.pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            default:
                throw new IllegalArgumentException("Budgets export only supports Excel and PDF formats");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/analytics")
    public ResponseEntity<byte[]> exportAnalytics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "3M") String timeRange,
            @RequestParam(defaultValue = "excel") String format) throws java.io.IOException {

        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format.toLowerCase()) {
            case "excel":
                data = exportService.exportAnalyticsExcel(userPrincipal.getId(), timeRange);
                filename = "analytics_" + timeRange + ".xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                data = exportService.exportAnalyticsPDF(userPrincipal.getId(), timeRange);
                filename = "analytics_" + timeRange + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            default:
                throw new IllegalArgumentException("Analytics export only supports Excel and PDF formats");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @PostMapping("/dashboard")
    public ResponseEntity<byte[]> exportDashboardWithImages(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody com.budgetwise.dto.ExportRequest request) throws java.io.IOException {

        byte[] data;
        String filename;
        MediaType mediaType;

        switch (request.getFormat().toLowerCase()) {
            case "excel":
                data = exportService.exportDashboardExcel(userPrincipal.getId(), request.getImages());
                filename = "dashboard.xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                data = exportService.exportDashboardPDF(userPrincipal.getId(), request.getImages());
                filename = "dashboard.pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            default:
                throw new IllegalArgumentException("Dashboard export only supports Excel and PDF formats");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @PostMapping("/analytics")
    public ResponseEntity<byte[]> exportAnalyticsWithImages(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody com.budgetwise.dto.ExportRequest request) throws java.io.IOException {

        byte[] data;
        String filename;
        MediaType mediaType;
        String timeRange = request.getTimeRange() != null ? request.getTimeRange() : "3M";

        switch (request.getFormat().toLowerCase()) {
            case "excel":
                data = exportService.exportAnalyticsExcel(userPrincipal.getId(), timeRange, request.getImages());
                filename = "analytics_" + timeRange + ".xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                data = exportService.exportAnalyticsPDF(userPrincipal.getId(), timeRange, request.getImages());
                filename = "analytics_" + timeRange + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            default:
                throw new IllegalArgumentException("Analytics export only supports Excel and PDF formats");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
