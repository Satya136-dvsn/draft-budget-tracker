package com.budgetwise.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Use simple in-memory cache (works without Redis)
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "dashboardSummary",
                "monthlyTrends",
                "categoryBreakdown",
                "categories",
                "userProfile",
                "predictions"
        );
        return cacheManager;
    }
}

// Note: To use Redis caching, uncomment Redis configuration in application.properties
// and replace this with RedisCacheManager configuration
