package com.budgetwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorizationSuggestionDto {
    private Long categoryId;
    private String categoryName;
    private Double confidence;
    private String reason;
}
