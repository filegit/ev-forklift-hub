package com.efh.community.controller;

import com.efh.common.result.Result;
import com.efh.community.service.CommentLikeService;
import com.efh.community.service.PostLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/like")
public class LikeController {
    
    @Autowired
    private PostLikeService postLikeService;
    
    @Autowired
    private CommentLikeService commentLikeService;
    
    /**
     * 帖子点赞或取消
     */
    @PostMapping("/post/{postId}")
    public Result<?> togglePostLike(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                    @PathVariable Long postId) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        postLikeService.toggleLike(userId, postId);
        return Result.success(null);
    }
    
    /**
     * 检查帖子是否已点赞
     */
    @GetMapping("/post/{postId}/check")
    public Result<Boolean> isPostLiked(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @PathVariable Long postId) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.success(false);
        }
        
        boolean liked = postLikeService.isLiked(userId, postId);
        return Result.success(liked);
    }
    
    /**
     * 评论点赞或取消
     */
    @PostMapping("/comment/{commentId}")
    public Result<?> toggleCommentLike(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @PathVariable Long commentId) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        commentLikeService.toggleLike(userId, commentId);
        return Result.success(null);
    }
    
    /**
     * 检查评论是否已点赞
     */
    @GetMapping("/comment/{commentId}/check")
    public Result<Boolean> isCommentLiked(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                          @PathVariable Long commentId) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.success(false);
        }
        
        boolean liked = commentLikeService.isLiked(userId, commentId);
        return Result.success(liked);
    }
}
