package com.budgetwise.service;

import com.budgetwise.dto.TransactionDto;
import com.budgetwise.entity.Category;
import com.budgetwise.entity.Transaction;
import com.budgetwise.exception.ResourceNotFoundException;
import com.budgetwise.repository.CategoryRepository;
import com.budgetwise.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetService budgetService;
    private final WebSocketService webSocketService;
    
    @Transactional
    public TransactionDto createTransaction(TransactionDto dto, Long userId) {
        // Validate category exists
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setCategoryId(dto.getCategoryId());
        transaction.setDescription(dto.getDescription());
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setIsAnomaly(false);
        
        Transaction saved = transactionRepository.save(transaction);
        
        // Update budget progress if expense
        if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
            budgetService.updateBudgetProgress(userId, transaction.getCategoryId());
        }
        
        // Send WebSocket notification
        webSocketService.sendDashboardUpdate(userId);
        
        return mapToDto(saved, category.getName());
    }
    
    public Page<TransactionDto> getTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
            .map(this::mapToDto);
    }
    
    public Page<TransactionDto> getTransactionsByFilters(
            Long userId,
            Transaction.TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable) {
        
        return transactionRepository.findByFilters(
            userId, type, categoryId, startDate, endDate, minAmount, maxAmount, pageable
        ).map(this::mapToDto);
    }
    
    public TransactionDto getTransactionById(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        
        return mapToDto(transaction);
    }
    
    @Transactional
    public TransactionDto updateTransaction(Long id, TransactionDto dto, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        
        // Validate edit within 30 days
        long daysSinceCreation = ChronoUnit.DAYS.between(transaction.getCreatedAt().toLocalDate(), LocalDate.now());
        if (daysSinceCreation > 30) {
            throw new IllegalArgumentException("Cannot edit transactions older than 30 days");
        }
        
        // Validate category exists
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        
        Transaction.TransactionType oldType = transaction.getType();
        Long oldCategoryId = transaction.getCategoryId();
        
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setCategoryId(dto.getCategoryId());
        transaction.setDescription(dto.getDescription());
        transaction.setTransactionDate(dto.getTransactionDate());
        
        Transaction updated = transactionRepository.save(transaction);
        
        // Update budget progress
        if (oldType == Transaction.TransactionType.EXPENSE) {
            budgetService.updateBudgetProgress(userId, oldCategoryId);
        }
        if (updated.getType() == Transaction.TransactionType.EXPENSE) {
            budgetService.updateBudgetProgress(userId, updated.getCategoryId());
        }
        
        return mapToDto(updated);
    }
    
    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        
        Transaction.TransactionType type = transaction.getType();
        Long categoryId = transaction.getCategoryId();
        
        transactionRepository.delete(transaction);
        
        // Update budget progress
        if (type == Transaction.TransactionType.EXPENSE) {
            budgetService.updateBudgetProgress(userId, categoryId);
        }
    }
    
    private TransactionDto mapToDto(Transaction transaction) {
        String categoryName = null;
        if (transaction.getCategoryId() != null) {
            categoryName = categoryRepository.findById(transaction.getCategoryId())
                .map(Category::getName)
                .orElse(null);
        }
        return mapToDto(transaction, categoryName);
    }
    
    private TransactionDto mapToDto(Transaction transaction, String categoryName) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setCategoryId(transaction.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setDescription(transaction.getDescription());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setIsAnomaly(transaction.getIsAnomaly());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }
}
