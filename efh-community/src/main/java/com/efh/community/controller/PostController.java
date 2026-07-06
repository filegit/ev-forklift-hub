package com.efh.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.common.utils.JwtUtil;
import com.efh.community.entity.Post;
import com.efh.community.service.PostService;
import com.efh.community.vo.PostVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 * 提供帖子发布、查询、点赞等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/post")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    /**
     * 发布帖子
     * 
     * POST /community/api/post
     * 
     * 请求头：
     * Authorization: Bearer {token}（网关会验证）
     * X-User-Id: {userId}（网关传递）
     * 
     * 请求体：
     * {
     *   "title": "帖子标题",
     *   "content": "帖子内容",
     *   "category": 1
     * }
     */
    @PostMapping
    public Result<?> createPost(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                @Validated @RequestBody PostVO postVO) {
        // 从网关传递的请求头中获取用户ID
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            log.error("用户ID为空，请求未通过网关或网关未传递用户ID");
            return Result.error(401, "未授权");
        }
        
        log.info("发布帖子请求: userId={}, title={}", userId, postVO.getTitle());
        
        postService.createPost(userId, postVO);
        
        return Result.success(null);
    }
    
    /**
     * 帖子列表（分页）
     * 
     * GET /community/api/post/list?page=1&size=10&category=1
     */
    @GetMapping("/list")
    public Result<IPage<Post>> getPostList(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) Integer category) {
        log.info("查询帖子列表: page={}, size={}, category={}", page, size, category);
        
        IPage<Post> postPage = postService.getPostList(new Page<>(page, size), category);
        
        return Result.success(postPage);
    }

    /**
     * 我的帖子
     */
    @GetMapping("/my")
    public Result<IPage<Post>> getMyPosts(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "20") Integer size) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        IPage<Post> postPage = postService.getMyPosts(userId, new Page<>(page, size));
        return Result.success(postPage);
    }
    
    /**
     * 帖子详情
     * 
     * GET /community/api/post/{id}
     */
    @GetMapping("/{id}")
    public Result<Post> getPostDetail(@PathVariable Long id) {
        log.info("查询帖子详情: postId={}", id);
        
        Post post = postService.getPostDetail(id);
        
        return Result.success(post);
    }
    
    /**
     * 点赞帖子
     * 
     * POST /community/api/post/{id}/like
     * 
     * 请求头：
     * Authorization: Bearer {token}（网关会验证）
     * X-User-Id: {userId}（网关传递）
     */
    @PostMapping("/{id}/like")
    public Result<?> likePost(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                              @PathVariable Long id) {
        // 从网关传递的请求头中获取用户ID
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            log.error("用户ID为空，请求未通过网关或网关未传递用户ID");
            return Result.error(401, "未授权");
        }
        
        log.info("点赞帖子: userId={}, postId={}", userId, id);
        
        postService.likePost(userId, id);
        
        return Result.success(null);
    }
    
    /**
     * 测试接口
     * 
     * GET /community/api/post/test
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("社区服务运行正常！");
    }
}
