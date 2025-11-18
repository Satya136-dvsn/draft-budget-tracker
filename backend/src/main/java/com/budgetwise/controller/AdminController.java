package com.budgetwise.controller;

import com.budgetwise.dto.AdminStatsDto;
import com.budgetwise.entity.AuditLog;
import com.budgetwise.entity.User;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getSystemStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AdminStatsDto stats = adminService.getSystemStats();
        
        // Log admin action
        adminService.logAdminAction(
            userPrincipal.getId(),
            "STATS_VIEW",
            null,
            "System",
            "Viewed system statistics",
            "127.0.0.1"
        );
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = adminService.getAllUsers(pageable);
        
        // Log admin action
        adminService.logAdminAction(
            userPrincipal.getId(),
            "USER_LIST",
            null,
            "User",
            "Listed all users (page: " + page + ", size: " + size + ")",
            "127.0.0.1"
        );
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = adminService.getAuditLogs(pageable);
        return ResponseEntity.ok(logs);
    }
}
