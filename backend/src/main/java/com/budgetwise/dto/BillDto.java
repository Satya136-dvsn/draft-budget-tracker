package com.budgetwise.dto;

import com.budgetwise.entity.Bill;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDto {

    private Long id;

    @NotBlank(message = "Bill name is required")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String category;

    @NotNull(message = "Recurrence type is required")
    private Bill.RecurrenceType recurrence;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private LocalDate nextDueDate;

    private Bill.BillStatus status;

    private Boolean autoReminder;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Calculated field for UI
    private Integer daysUntilDue;
}
