package com.budgetwise.repository;

import com.budgetwise.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Optional<Post> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT p FROM Post p ORDER BY (p.likeCount + p.commentCount) DESC")
    Page<Post> findTrendingPosts(Pageable pageable);
}
