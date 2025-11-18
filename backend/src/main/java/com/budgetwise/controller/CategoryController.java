package com.budgetwise.controller;

import com.budgetwise.dto.CategoryDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Get all categories for the current user (system + custom)
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<CategoryDto> categories = categoryService.getAllCategoriesForUser(currentUser.getId());
        return ResponseEntity.ok(categories);
    }

    /**
     * Get only user's custom categories
     */
    @GetMapping("/custom")
    public ResponseEntity<List<CategoryDto>> getCustomCategories(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<CategoryDto> categories = categoryService.getUserCustomCategories(currentUser.getId());
        return ResponseEntity.ok(categories);
    }

    /**
     * Get a specific category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CategoryDto category = categoryService.getCategoryById(id, currentUser.getId());
        return ResponseEntity.ok(category);
    }

    /**
     * Create a new custom category
     */
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CategoryDto createdCategory = categoryService.createCategory(categoryDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    /**
     * Update an existing custom category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto, currentUser.getId());
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Delete a custom category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        categoryService.deleteCategory(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Test endpoint to verify category controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Category endpoint is working!");
    }
}
