package com.budgetwise.controller;

import com.budgetwise.dto.InvestmentDto;
import com.budgetwise.dto.PortfolioSummaryDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<InvestmentDto> createInvestment(
            @Valid @RequestBody InvestmentDto investmentDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        InvestmentDto created = investmentService.createInvestment(investmentDto, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<InvestmentDto>> getAllInvestments(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<InvestmentDto> investments = investmentService.getAllInvestments(userPrincipal.getId());
        return ResponseEntity.ok(investments);
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummaryDto> getPortfolioSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        PortfolioSummaryDto summary = investmentService.getPortfolioSummary(userPrincipal.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentDto> getInvestmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        InvestmentDto investment = investmentService.getInvestmentById(id, userPrincipal.getId());
        return ResponseEntity.ok(investment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentDto> updateInvestment(
            @PathVariable Long id,
            @Valid @RequestBody InvestmentDto investmentDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        InvestmentDto updated = investmentService.updateInvestment(id, investmentDto, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<InvestmentDto> updateCurrentPrice(
            @PathVariable Long id,
            @RequestParam BigDecimal currentPrice,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        InvestmentDto updated = investmentService.updateCurrentPrice(id, currentPrice, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        investmentService.deleteInvestment(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
