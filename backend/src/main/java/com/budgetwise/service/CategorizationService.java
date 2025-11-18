package com.budgetwise.service;

import com.budgetwise.dto.CategorizationSuggestionDto;
import com.budgetwise.entity.Category;
import com.budgetwise.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategorizationService {

    private final CategoryRepository categoryRepository;

    // Keyword dictionary for categorization
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new HashMap<>() {{
        put("Food & Dining", Arrays.asList("restaurant", "cafe", "coffee", "pizza", "burger", "food", "dining",
                "mcdonald", "starbucks", "subway", "chipotle", "domino", "kfc", "taco", "wendy"));
        put("Groceries", Arrays.asList("grocery", "supermarket", "walmart", "target", "costco", "safeway",
                "kroger", "whole foods", "trader joe", "aldi", "market"));
        put("Transportation", Arrays.asList("uber", "lyft", "taxi", "gas", "fuel", "shell", "chevron", "exxon",
                "bp", "parking", "metro", "transit", "bus", "train"));
        put("Rent", Arrays.asList("rent", "lease", "apartment", "housing", "landlord"));
        put("Utilities", Arrays.asList("electric", "water", "gas", "internet", "phone", "utility", "verizon",
                "at&t", "comcast", "spectrum", "t-mobile"));
        put("Healthcare", Arrays.asList("doctor", "hospital", "pharmacy", "medical", "health", "clinic",
                "cvs", "walgreens", "medicine", "prescription"));
        put("Entertainment", Arrays.asList("movie", "cinema", "netflix", "spotify", "hulu", "disney", "game",
                "theater", "concert", "ticket", "entertainment"));
        put("Shopping", Arrays.asList("amazon", "ebay", "shop", "store", "mall", "clothing", "fashion",
                "nike", "adidas", "zara", "h&m"));
        put("Education", Arrays.asList("school", "university", "college", "course", "tuition", "book",
                "education", "learning", "udemy", "coursera"));
        put("Travel", Arrays.asList("hotel", "flight", "airline", "airbnb", "booking", "expedia", "travel",
                "vacation", "trip", "airport"));
        put("Insurance", Arrays.asList("insurance", "policy", "premium", "geico", "state farm", "allstate"));
    }};

    public CategorizationSuggestionDto suggestCategory(String description, Long userId) {
        if (description == null || description.trim().isEmpty()) {
            return getDefaultSuggestion(userId);
        }

        String lowerDesc = description.toLowerCase();

        // Get all categories for user
        List<Category> categories = categoryRepository.findByUserIdOrIsSystemTrue(userId);

        // Try to match keywords
        Map<String, Double> categoryScores = new HashMap<>();

        for (Category category : categories) {
            double score = calculateMatchScore(lowerDesc, category.getName());
            if (score > 0) {
                categoryScores.put(category.getName(), score);
            }
        }

        // Find best match
        if (!categoryScores.isEmpty()) {
            String bestCategory = categoryScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (bestCategory != null) {
                Category category = categories.stream()
                        .filter(c -> c.getName().equals(bestCategory))
                        .findFirst()
                        .orElse(null);

                if (category != null) {
                    double confidence = Math.min(100, categoryScores.get(bestCategory) * 100);
                    return CategorizationSuggestionDto.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .confidence(confidence)
                            .reason(String.format("Matched keywords in description: '%s'", description))
                            .build();
                }
            }
        }

        // No match found, return default
        return getDefaultSuggestion(userId);
    }

    private double calculateMatchScore(String description, String categoryName) {
        List<String> keywords = CATEGORY_KEYWORDS.getOrDefault(categoryName, new ArrayList<>());

        int matchCount = 0;
        for (String keyword : keywords) {
            if (description.contains(keyword)) {
                matchCount++;
            }
        }

        return keywords.isEmpty() ? 0 : (double) matchCount / keywords.size();
    }

    private CategorizationSuggestionDto getDefaultSuggestion(Long userId) {
        // Return "Other Expense" as default
        Category defaultCategory = categoryRepository.findByUserIdOrIsSystemTrue(userId).stream()
                .filter(c -> c.getName().equals("Other Expense"))
                .findFirst()
                .orElse(null);

        if (defaultCategory != null) {
            return CategorizationSuggestionDto.builder()
                    .categoryId(defaultCategory.getId())
                    .categoryName(defaultCategory.getName())
                    .confidence(30.0)
                    .reason("No specific keywords matched, using default category")
                    .build();
        }

        return null;
    }

    public void learnFromCorrection(String description, Long categoryId, Long userId) {
        // In a production system, this would store the correction in a learning database
        // For now, this is a placeholder for future ML implementation
        // Could store: description -> categoryId mappings for user-specific learning
    }
}
