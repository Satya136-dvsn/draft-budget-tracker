package com.budgetwise.controller;

import com.budgetwise.service.ExportService;
import com.budgetwise.service.ProfileService;
import com.budgetwise.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gdpr")
@RequiredArgsConstructor
public class GdprController {

    private final ExportService exportService;
    private final UserService userService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUserData(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.findByEmail(userDetails.getUsername()).getId();
        byte[] data = exportService.exportAllDataCSV(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_data_export.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String password) {
        Long userId = userService.findByEmail(userDetails.getUsername()).getId();
        userService.deleteUserAccount(userId, password);
        return ResponseEntity.ok("Account deleted successfully. We're sorry to see you go.");
    }
}
