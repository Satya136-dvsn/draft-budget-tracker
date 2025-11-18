package com.budgetwise.dto;

import com.budgetwise.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryDto {

    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Category type is required")
    private Category.CategoryType type;

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;

    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;

    private Boolean isSystem;

    private Long userId;

    // Constructors
    public CategoryDto() {
    }

    public CategoryDto(Long id, String name, Category.CategoryType type, String icon, String color, Boolean isSystem, Long userId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isSystem = isSystem;
        this.userId = userId;
    }

    // Static factory method to create DTO from entity
    public static CategoryDto fromEntity(Category category) {
        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getType(),
            category.getIcon(),
            category.getColor(),
            category.getIsSystem(),
            category.getUser() != null ? category.getUser().getId() : null
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category.CategoryType getType() {
        return type;
    }

    public void setType(Category.CategoryType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
