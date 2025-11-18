package com.budgetwise.controller;

import com.budgetwise.dto.AnomalyDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.AnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;

    @GetMapping
    public ResponseEntity<List<AnomalyDto>> detectAnomalies(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<AnomalyDto> anomalies = anomalyDetectionService.detectAnomalies(userPrincipal.getId());
        return ResponseEntity.ok(anomalies);
    }

    @PostMapping("/{transactionId}/mark-expected")
    public ResponseEntity<Void> markAsExpected(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long transactionId) {
        anomalyDetectionService.markAsExpected(transactionId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
}
