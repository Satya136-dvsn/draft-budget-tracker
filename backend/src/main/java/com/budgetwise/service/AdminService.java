package com.budgetwise.service;

import com.budgetwise.dto.AdminStatsDto;
import com.budgetwise.entity.AuditLog;
import com.budgetwise.entity.User;
import com.budgetwise.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminStatsDto getSystemStats() {
        long totalUsers = userRepository.count();
        long totalTransactions = transactionRepository.count();
        long totalCategories = categoryRepository.count();

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalTransactions(totalTransactions)
                .totalCategories(totalCategories)
                .build();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void logAdminAction(Long adminUserId, String actionType, Long targetUserId, 
                                String targetResource, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .adminUserId(adminUserId)
                .actionType(actionType)
                .targetUserId(targetUserId)
                .targetResource(targetResource)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
