package com.budgetwise.repository;

import com.budgetwise.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find all system categories
     */
    List<Category> findByIsSystemTrue();
    
    /**
     * Find all categories for a user (system + user's custom categories)
     */
    @Query("SELECT c FROM Category c WHERE c.isSystem = true OR c.user.id = :userId")
    List<Category> findAllByUserIdIncludingSystem(@Param("userId") Long userId);
    
    /**
     * Find only user's custom categories
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isSystem = false")
    List<Category> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find category by name and type for a user
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.type = :type AND (c.isSystem = true OR c.user.id = :userId)")
    Optional<Category> findByNameAndTypeForUser(@Param("name") String name, 
                                                  @Param("type") Category.CategoryType type,
                                                  @Param("userId") Long userId);
    
    /**
     * Check if a category name already exists for a user
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.type = :type AND (c.isSystem = true OR c.user.id = :userId)")
    boolean existsByNameAndTypeForUser(@Param("name") String name,
                                        @Param("type") Category.CategoryType type,
                                        @Param("userId") Long userId);
    
    /**
     * Find categories by userId or system categories
     */
    @Query("SELECT c FROM Category c WHERE c.isSystem = true OR c.user.id = :userId")
    List<Category> findByUserIdOrIsSystemTrue(@Param("userId") Long userId);
}
