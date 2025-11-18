package com.budgetwise.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProfileDto {

    private Long id;

    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;

    @DecimalMin(value = "0.0", inclusive = false, message = "Savings target must be positive")
    private BigDecimal savingsTarget;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;

    @Pattern(regexp = "light|dark", message = "Theme must be either 'light' or 'dark'")
    private String theme;

    @Size(max = 20, message = "Date format must not exceed 20 characters")
    private String dateFormat;

    private Boolean notificationEmail;

    private Boolean notificationPush;

    // Constructors
    public ProfileDto() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getSavingsTarget() {
        return savingsTarget;
    }

    public void setSavingsTarget(BigDecimal savingsTarget) {
        this.savingsTarget = savingsTarget;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Boolean getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(Boolean notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    public Boolean getNotificationPush() {
        return notificationPush;
    }

    public void setNotificationPush(Boolean notificationPush) {
        this.notificationPush = notificationPush;
    }
}
