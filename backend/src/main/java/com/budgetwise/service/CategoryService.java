package com.budgetwise.service;

import com.budgetwise.dto.CategoryDto;
import com.budgetwise.entity.Category;
import com.budgetwise.entity.User;
import com.budgetwise.repository.CategoryRepository;
import com.budgetwise.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final com.budgetwise.repository.TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository,
            com.budgetwise.repository.TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Seed system categories on application startup
     */
    @PostConstruct
    @Transactional
    public void seedSystemCategories() {
        List<Category> existingSystemCategories = categoryRepository.findByIsSystemTrue();

        if (existingSystemCategories.isEmpty()) {
            // Expense categories
            createSystemCategory("Food & Dining", Category.CategoryType.EXPENSE, "🍽️", "#FF6B6B");
            createSystemCategory("Groceries", Category.CategoryType.EXPENSE, "🛒", "#4ECDC4");
            createSystemCategory("Transportation", Category.CategoryType.EXPENSE, "🚗", "#45B7D1");
            createSystemCategory("Rent", Category.CategoryType.EXPENSE, "🏠", "#96CEB4");
            createSystemCategory("Utilities", Category.CategoryType.EXPENSE, "💡", "#FFEAA7");
            createSystemCategory("Healthcare", Category.CategoryType.EXPENSE, "🏥", "#DFE6E9");
            createSystemCategory("Entertainment", Category.CategoryType.EXPENSE, "🎬", "#A29BFE");
            createSystemCategory("Shopping", Category.CategoryType.EXPENSE, "🛍️", "#FD79A8");
            createSystemCategory("Travel", Category.CategoryType.EXPENSE, "✈️", "#6C5CE7");
            createSystemCategory("Education", Category.CategoryType.EXPENSE, "📚", "#00B894");
            createSystemCategory("Insurance", Category.CategoryType.EXPENSE, "🛡️", "#0984E3");
            createSystemCategory("Personal Care", Category.CategoryType.EXPENSE, "💅", "#FDCB6E");
            createSystemCategory("Gifts & Donations", Category.CategoryType.EXPENSE, "🎁", "#E17055");
            createSystemCategory("Bills & EMI", Category.CategoryType.EXPENSE, "📄", "#B2BEC3");
            createSystemCategory("Other Expenses", Category.CategoryType.EXPENSE, "📦", "#636E72");

            // Income categories
            createSystemCategory("Salary", Category.CategoryType.INCOME, "💰", "#00B894");
            createSystemCategory("Business Income", Category.CategoryType.INCOME, "💼", "#0984E3");
            createSystemCategory("Freelance", Category.CategoryType.INCOME, "💻", "#6C5CE7");
            createSystemCategory("Investments", Category.CategoryType.INCOME, "📈", "#FDCB6E");
            createSystemCategory("Rental Income", Category.CategoryType.INCOME, "🏘️", "#00CEC9");
            createSystemCategory("Gifts Received", Category.CategoryType.INCOME, "🎁", "#FD79A8");
            createSystemCategory("Refunds", Category.CategoryType.INCOME, "↩️", "#74B9FF");
            createSystemCategory("Other Income", Category.CategoryType.INCOME, "💵", "#55EFC4");

            System.out.println("✅ System categories seeded successfully!");
        }
    }

    private void createSystemCategory(String name, Category.CategoryType type, String icon, String color) {
        Category category = new Category(name, type, true);
        category.setIcon(icon);
        category.setColor(color);
        categoryRepository.save(category);
    }

    /**
     * Get all categories for a user (system + custom)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#userId")
    public List<CategoryDto> getAllCategoriesForUser(Long userId) {
        List<Category> categories = categoryRepository.findAllByUserIdIncludingSystem(userId);
        return categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get only user's custom categories
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "custom_categories", key = "#userId")
    public List<CategoryDto> getUserCustomCategories(Long userId) {
        List<Category> categories = categoryRepository.findByUserId(userId);
        return categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        // Check if user has access to this category
        if (!category.getIsSystem() && !category.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this category");
        }

        return CategoryDto.fromEntity(category);
    }

    /**
     * Create a custom category for a user
     */
    @Transactional
    @CacheEvict(value = { "categories", "custom_categories" }, allEntries = true)
    public CategoryDto createCategory(CategoryDto categoryDto, Long userId) {
        // Check if category name already exists for this user
        if (categoryRepository.existsByNameAndTypeForUser(categoryDto.getName(), categoryDto.getType(), userId)) {
            throw new RuntimeException("Category with name '" + categoryDto.getName() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setType(categoryDto.getType());
        category.setIcon(categoryDto.getIcon());
        category.setColor(categoryDto.getColor());
        category.setIsSystem(false); // User categories are never system categories
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);
        return CategoryDto.fromEntity(savedCategory);
    }

    /**
     * Update a category (only custom categories can be updated)
     */
    @Transactional
    @CacheEvict(value = { "categories", "custom_categories" }, allEntries = true)
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        // Prevent modification of system categories
        if (category.getIsSystem()) {
            throw new RuntimeException("System categories cannot be modified");
        }

        // Check if user owns this category
        if (!category.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this category");
        }

        // Check if new name conflicts with existing categories
        if (!category.getName().equals(categoryDto.getName())) {
            if (categoryRepository.existsByNameAndTypeForUser(categoryDto.getName(), categoryDto.getType(), userId)) {
                throw new RuntimeException("Category with name '" + categoryDto.getName() + "' already exists");
            }
        }

        category.setName(categoryDto.getName());
        category.setIcon(categoryDto.getIcon());
        category.setColor(categoryDto.getColor());

        Category updatedCategory = categoryRepository.save(category);
        return CategoryDto.fromEntity(updatedCategory);
    }

    /**
     * Delete a category (only custom categories can be deleted)
     */
    @Transactional
    @CacheEvict(value = { "categories", "custom_categories" }, allEntries = true)
    public void deleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        // Prevent deletion of system categories
        if (category.getIsSystem()) {
            throw new RuntimeException("System categories cannot be deleted");
        }

        // Check if user owns this category
        if (!category.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this category");
        }

        // Check if category is used in any transactions
        if (transactionRepository.existsByCategoryId(id)) {
            throw new RuntimeException(
                    "Cannot delete category because it has associated transactions. Please reassign them first.");
        }

        categoryRepository.delete(category);
    }
}
