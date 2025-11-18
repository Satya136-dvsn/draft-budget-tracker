package com.budgetwise.controller;

import com.budgetwise.dto.CommentDto;
import com.budgetwise.dto.PostDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PostDto postDto) {
        PostDto created = forumService.createPost(postDto, userPrincipal.getId());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = forumService.getPosts(pageable, sort, userPrincipal.getId());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDto> getPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        PostDto post = forumService.getPost(id, userPrincipal.getId());
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Void> likePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        forumService.likePost(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<Void> unlikePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        forumService.unlikePost(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> addComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @RequestBody CommentDto commentDto) {
        CommentDto created = forumService.addComment(postId, commentDto, userPrincipal.getId());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDto> comments = forumService.getComments(postId, pageable);
        return ResponseEntity.ok(comments);
    }
}
