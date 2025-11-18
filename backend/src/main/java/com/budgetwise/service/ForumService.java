package com.budgetwise.service;

import com.budgetwise.dto.CommentDto;
import com.budgetwise.dto.PostDto;
import com.budgetwise.entity.Comment;
import com.budgetwise.entity.Like;
import com.budgetwise.entity.Post;
import com.budgetwise.repository.CommentRepository;
import com.budgetwise.repository.LikeRepository;
import com.budgetwise.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public Page<PostDto> getPosts(Pageable pageable, String sort, Long userId) {
        if ("trending".equals(sort)) {
            return postRepository.findTrendingPosts(pageable)
                    .map(post -> convertToPostDto(post, userId));
        }
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> convertToPostDto(post, userId));
    }

    public PostDto getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToPostDto(post, userId);
    }

    public PostDto createPost(PostDto postDto, Long userId) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setTags(postDto.getTags());
        post.setLikeCount(0);
        post.setCommentCount(0);
        
        Post saved = postRepository.save(post);
        return convertToPostDto(saved, userId);
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (likeRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, postId).isEmpty()) {
            Like like = new Like();
            like.setUserId(userId);
            like.setPostId(postId);
            like.setType("POST");
            likeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
        }
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        likeRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, postId).ifPresent(like -> {
            likeRepository.delete(like);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postRepository.save(post);
        });
    }



    @Transactional
    public CommentDto addComment(Long postId, CommentDto commentDto, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(commentDto.getContent());
        comment.setLikeCount(0);
        
        Comment saved = commentRepository.save(comment);
        
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        
        return convertToCommentDto(saved);
    }

    public Page<CommentDto> getComments(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(this::convertToCommentDto);
    }

    private PostDto convertToPostDto(Post post, Long userId) {
        boolean isLiked = likeRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, post.getId()).isPresent();
        
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .tags(post.getTags())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .userId(post.getUserId())
                .userName("User " + post.getUserId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isLiked(isLiked)
                .build();
    }

    private CommentDto convertToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .userId(comment.getUserId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
