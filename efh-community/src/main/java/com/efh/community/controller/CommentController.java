package com.efh.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.community.entity.Comment;
import com.efh.community.service.CommentService;
import com.efh.community.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 * 提供评论发表、查询、删除等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 发表评论
     * 
     * POST /community/api/comment
     * 
     * 请求头：
     * Authorization: Bearer {token}（网关会验证）
     * X-User-Id: {userId}（网关传递）
     * 
     * 请求体：
     * {
     *   "postId": 1,
     *   "content": "评论内容",
     *   "parentId": 0
     * }
     */
    @PostMapping
    public Result<?> createComment(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                   @Validated @RequestBody CommentVO commentVO) {
        // 从网关传递的请求头中获取用户ID
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            log.error("用户ID为空，请求未通过网关或网关未传递用户ID");
            return Result.error(401, "未授权");
        }
        
        log.info("发表评论请求: userId={}, postId={}", userId, commentVO.getPostId());
        
        commentService.createComment(userId, commentVO);
        
        return Result.success(null);
    }
    
    /**
     * 获取帖子的评论列表
     * 
     * GET /community/api/comment/list?postId=1&page=1&size=10
     */
    @GetMapping("/list")
    public Result<IPage<Comment>> getCommentList(@RequestParam Long postId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询评论列表: postId={}, page={}, size={}", postId, page, size);
        
        IPage<Comment> commentPage = commentService.getCommentList(postId, new Page<>(page, size));
        
        return Result.success(commentPage);
    }

    /**
     * 我的评论
     */
    @GetMapping("/my")
    public Result<IPage<Comment>> getMyComments(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "20") Integer size) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        IPage<Comment> commentPage = commentService.getMyComments(userId, new Page<>(page, size));
        return Result.success(commentPage);
    }
    
    /**
     * 删除评论
     * 
     * DELETE /community/api/comment/{id}
     * 
     * 请求头：
     * Authorization: Bearer {token}（网关会验证）
     * X-User-Id: {userId}（网关传递）
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteComment(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                   @PathVariable Long id) {
        // 从网关传递的请求头中获取用户ID
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            log.error("用户ID为空，请求未通过网关或网关未传递用户ID");
            return Result.error(401, "未授权");
        }
        
        log.info("删除评论请求: userId={}, commentId={}", userId, id);
        
        commentService.deleteComment(userId, id);
        
        return Result.success(null);
    }
}
