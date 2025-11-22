package com.budgetwise.controller;

import com.budgetwise.dto.BillDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<BillDto> createBill(
            @Valid @RequestBody BillDto billDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BillDto created = billService.createBill(billDto, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BillDto>> getAllBills(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<BillDto> bills = billService.getAllBills(userPrincipal.getId());
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<BillDto>> getUpcomingBills(
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<BillDto> bills = billService.getUpcomingBills(userPrincipal.getId(), days);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDto> getBillById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BillDto bill = billService.getBillById(id, userPrincipal.getId());
        return ResponseEntity.ok(bill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillDto> updateBill(
            @PathVariable Long id,
            @Valid @RequestBody BillDto billDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BillDto updated = billService.updateBill(id, billDto, userPrincipal.getId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<BillDto> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BillDto paid = billService.markAsPaid(id, userPrincipal.getId());
        return ResponseEntity.ok(paid);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        billService.deleteBill(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
