package com.budgetwise.service;

import com.budgetwise.dto.BillDto;
import com.budgetwise.entity.Bill;
import com.budgetwise.entity.Transaction;
import com.budgetwise.exception.ResourceNotFoundException;
import com.budgetwise.repository.BillRepository;
import com.budgetwise.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public BillDto createBill(BillDto dto, Long userId) {
        Bill bill = new Bill();
        bill.setUserId(userId);
        bill.setName(dto.getName());
        bill.setAmount(dto.getAmount());
        bill.setCategory(dto.getCategory());
        bill.setRecurrence(dto.getRecurrence());
        bill.setDueDate(dto.getDueDate());
        bill.setNextDueDate(dto.getDueDate()); // Initial next due date is the due date
        bill.setStatus(Bill.BillStatus.PENDING);
        bill.setAutoReminder(dto.getAutoReminder() != null ? dto.getAutoReminder() : true);
        bill.setNotes(dto.getNotes());

        Bill saved = billRepository.save(bill);
        return mapToDto(saved);
    }

    public List<BillDto> getAllBills(Long userId) {
        // Update overdue bills first
        updateOverdueBills(userId);

        return billRepository.findByUserIdOrderByNextDueDateAsc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BillDto> getUpcomingBills(Long userId, int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);

        return billRepository.findByUserIdAndNextDueDateBetween(userId, start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BillDto getBillById(Long id, Long userId) {
        Bill bill = billRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        return mapToDto(bill);
    }

    @Transactional
    public BillDto updateBill(Long id, BillDto dto, Long userId) {
        Bill bill = billRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        bill.setName(dto.getName());
        bill.setAmount(dto.getAmount());
        bill.setCategory(dto.getCategory());
        bill.setRecurrence(dto.getRecurrence());
        bill.setDueDate(dto.getDueDate());
        if (dto.getAutoReminder() != null) {
            bill.setAutoReminder(dto.getAutoReminder());
        }
        bill.setNotes(dto.getNotes());

        Bill updated = billRepository.save(bill);
        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = { "dashboard_summary", "dashboard_trends", "dashboard_breakdown" }, allEntries = true)
    public BillDto markAsPaid(Long id, Long userId) {
        Bill bill = billRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        // Create transaction for payment
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(Transaction.TransactionType.EXPENSE);
        transaction.setAmount(bill.getAmount());
        transaction.setCategoryId(null); // Could map category name to ID if needed
        transaction.setDescription("Bill Payment: " + bill.getName());
        transaction.setTransactionDate(LocalDate.now());
        transaction.setIsAnomaly(false);

        transactionRepository.save(transaction);

        // Update bill status and calculate next due date
        bill.setStatus(Bill.BillStatus.PAID);

        // Calculate next due date based on recurrence
        if (bill.getRecurrence() != Bill.RecurrenceType.ONE_TIME) {
            LocalDate nextDue = calculateNextDueDate(bill.getNextDueDate(), bill.getRecurrence());
            bill.setNextDueDate(nextDue);
            bill.setStatus(Bill.BillStatus.PENDING); // Reset for next occurrence
        }

        Bill updated = billRepository.save(bill);
        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = { "dashboard_summary", "dashboard_trends", "dashboard_breakdown" }, allEntries = true)
    public void deleteBill(Long id, Long userId) {
        Bill bill = billRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        billRepository.delete(bill);
    }

    private LocalDate calculateNextDueDate(LocalDate currentDueDate, Bill.RecurrenceType recurrence) {
        switch (recurrence) {
            case WEEKLY:
                return currentDueDate.plusWeeks(1);
            case MONTHLY:
                return currentDueDate.plusMonths(1);
            case QUARTERLY:
                return currentDueDate.plusMonths(3);
            case YEARLY:
                return currentDueDate.plusYears(1);
            case ONE_TIME:
            default:
                return currentDueDate;
        }
    }

    private void updateOverdueBills(Long userId) {
        LocalDate today = LocalDate.now();
        List<Bill> overdueBills = billRepository.findByUserIdAndNextDueDateBeforeAndStatus(
                userId, today, Bill.BillStatus.PENDING);

        for (Bill bill : overdueBills) {
            bill.setStatus(Bill.BillStatus.OVERDUE);
        }

        if (!overdueBills.isEmpty()) {
            billRepository.saveAll(overdueBills);
        }
    }

    private BillDto mapToDto(Bill bill) {
        BillDto dto = BillDto.builder()
                .id(bill.getId())
                .name(bill.getName())
                .amount(bill.getAmount())
                .category(bill.getCategory())
                .recurrence(bill.getRecurrence())
                .dueDate(bill.getDueDate())
                .nextDueDate(bill.getNextDueDate())
                .status(bill.getStatus())
                .autoReminder(bill.getAutoReminder())
                .notes(bill.getNotes())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();

        // Calculate days until due
        if (bill.getNextDueDate() != null) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), bill.getNextDueDate());
            dto.setDaysUntilDue((int) daysUntil);
        }

        return dto;
    }
}
