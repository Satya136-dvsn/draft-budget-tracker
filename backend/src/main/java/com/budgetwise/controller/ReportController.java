package com.budgetwise.controller;

import com.budgetwise.dto.ReportDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final com.budgetwise.service.ExportService exportService;

    @PostMapping("/generate/{templateId}")
    public ResponseEntity<byte[]> generateTemplateReport(
            @PathVariable int templateId,
            @RequestParam(defaultValue = "text") String format,
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws java.io.IOException {

        if ("excel".equalsIgnoreCase(format)) {
            java.time.LocalDate startDate = java.time.LocalDate.now().withDayOfMonth(1);
            java.time.LocalDate endDate = java.time.LocalDate.now();

            if (templateId == 2) { // Tax Report (Last Year)
                startDate = java.time.LocalDate.now().minusYears(1).withDayOfYear(1);
                endDate = java.time.LocalDate.now().minusYears(1)
                        .withDayOfYear(java.time.LocalDate.now().minusYears(1).lengthOfYear());
            }

            byte[] content = exportService.exportTransactionsExcel(userPrincipal.getId(), startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } else if ("pdf".equalsIgnoreCase(format)) {
            java.time.LocalDate startDate = java.time.LocalDate.now().withDayOfMonth(1);
            java.time.LocalDate endDate = java.time.LocalDate.now();

            if (templateId == 2) { // Tax Report (Last Year)
                startDate = java.time.LocalDate.now().minusYears(1).withDayOfYear(1);
                endDate = java.time.LocalDate.now().minusYears(1)
                        .withDayOfYear(java.time.LocalDate.now().minusYears(1).lengthOfYear());
            }

            byte[] content = exportService.exportTransactionsPDF(userPrincipal.getId(), startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(content);
        }

        byte[] content = reportService.generateTemplateReport(userPrincipal.getId(), templateId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @PostMapping("/custom")
    public ResponseEntity<byte[]> generateCustomReport(
            @RequestBody ReportDto.CustomReportConfig config,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        byte[] content = reportService.generateCustomReport(userPrincipal.getId(), config);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + config.getName() + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<ReportDto.ScheduledReportDto>> getScheduledReports(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(reportService.getScheduledReports(userPrincipal.getId()));
    }

    @PostMapping("/schedule")
    public ResponseEntity<ReportDto.ScheduledReportDto> createScheduledReport(
            @RequestBody ReportDto.ScheduledReportDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(reportService.createScheduledReport(userPrincipal.getId(), dto));
    }

    @PutMapping("/schedule/{id}")
    public ResponseEntity<ReportDto.ScheduledReportDto> updateScheduledReport(
            @PathVariable Long id,
            @RequestBody ReportDto.ScheduledReportDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(reportService.updateScheduledReport(userPrincipal.getId(), id, dto));
    }

    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<Void> deleteScheduledReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        reportService.deleteScheduledReport(userPrincipal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/schedule/{id}/run")
    public ResponseEntity<Void> runScheduledReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // In a real app, this would trigger email sending
        // For now, we just acknowledge the request
        return ResponseEntity.ok().build();
    }
}
