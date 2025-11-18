package com.budgetwise.controller;

import com.budgetwise.dto.TransactionDto;
import com.budgetwise.entity.Transaction;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TransactionDto created = transactionService.createTransaction(dto, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    public ResponseEntity<Page<TransactionDto>> getTransactions(
            @RequestParam(required = false) Transaction.TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TransactionDto> transactions;
        if (type != null || categoryId != null || startDate != null || 
            endDate != null || minAmount != null || maxAmount != null) {
            transactions = transactionService.getTransactionsByFilters(
                userPrincipal.getId(), type, categoryId, startDate, endDate, 
                minAmount, maxAmount, pageable
            );
        } else {
            transactions = transactionService.getTransactions(userPrincipal.getId(), pageable);
        }
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TransactionDto transaction = transactionService.getTransactionById(id, userPrincipal.getId());
        return ResponseEntity.ok(transaction);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TransactionDto updated = transactionService.updateTransaction(id, dto, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        transactionService.deleteTransaction(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
