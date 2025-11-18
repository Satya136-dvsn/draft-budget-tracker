package com.budgetwise.service;

import com.budgetwise.config.ExternalApiConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {
    
    private final ExternalApiConfig apiConfig;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    
    /**
     * Get financial advice from OpenAI GPT
     */
    public String getFinancialAdvice(String userContext, String question) {
        try {
            String prompt = buildPrompt(userContext, question);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-4");
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("max_tokens", 500);
            requestBody.addProperty("temperature", 0.7);
            
            Request request = new Request.Builder()
                .url(apiConfig.getOpenaiApiUrl() + "/completions")
                .addHeader("Authorization", "Bearer " + apiConfig.getOpenaiApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    gson.toJson(requestBody),
                    MediaType.parse("application/json")
                ))
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
                    return jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString().trim();
                } else {
                    log.error("OpenAI API error: {}", response.code());
                    return getFallbackAdvice(question);
                }
            }
        } catch (IOException e) {
            log.error("Error calling OpenAI API", e);
            return getFallbackAdvice(question);
        }
    }
    
    /**
     * Categorize transaction using AI
     */
    public String categorizeTransaction(String description) {
        try {
            String prompt = "Categorize this transaction into one of these categories: " +
                "Food & Dining, Groceries, Transportation, Rent, Utilities, Healthcare, " +
                "Entertainment, Shopping, Education, Travel, Insurance, Other. " +
                "Transaction: " + description + ". " +
                "Respond with only the category name.";
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-4");
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("max_tokens", 20);
            requestBody.addProperty("temperature", 0.3);
            
            Request request = new Request.Builder()
                .url(apiConfig.getOpenaiApiUrl() + "/completions")
                .addHeader("Authorization", "Bearer " + apiConfig.getOpenaiApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    gson.toJson(requestBody),
                    MediaType.parse("application/json")
                ))
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
                    return jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString().trim();
                }
            }
        } catch (IOException e) {
            log.error("Error categorizing transaction with AI", e);
        }
        
        return "Other";
    }
    
    private String buildPrompt(String userContext, String question) {
        return "You are a financial advisor. Based on the following user financial data:\n" +
            userContext + "\n\n" +
            "User question: " + question + "\n\n" +
            "Provide helpful, actionable financial advice:";
    }
    
    private String getFallbackAdvice(String question) {
        // Rule-based fallback advice
        if (question.toLowerCase().contains("save")) {
            return "Consider following the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings.";
        } else if (question.toLowerCase().contains("budget")) {
            return "Track your expenses regularly and set realistic budget limits for each category.";
        } else if (question.toLowerCase().contains("debt")) {
            return "Focus on paying off high-interest debt first while maintaining minimum payments on others.";
        }
        return "Review your spending patterns and look for areas where you can reduce unnecessary expenses.";
    }
}
