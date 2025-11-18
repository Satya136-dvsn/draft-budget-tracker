package com.budgetwise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import okhttp3.OkHttpClient;

import java.time.Duration;

@Configuration
public class ExternalApiConfig {
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    @Value("${openai.api.url}")
    private String openaiApiUrl;
    
    @Value("${alphavantage.api.key}")
    private String alphaVantageApiKey;
    
    @Value("${alphavantage.api.url}")
    private String alphaVantageApiUrl;
    
    @Value("${dropbox.api.key}")
    private String dropboxApiKey;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    // Getters for API keys and URLs
    public String getOpenaiApiKey() {
        return openaiApiKey;
    }
    
    public String getOpenaiApiUrl() {
        return openaiApiUrl;
    }
    
    public String getAlphaVantageApiKey() {
        return alphaVantageApiKey;
    }
    
    public String getAlphaVantageApiUrl() {
        return alphaVantageApiUrl;
    }
    
    public String getDropboxApiKey() {
        return dropboxApiKey;
    }
}
