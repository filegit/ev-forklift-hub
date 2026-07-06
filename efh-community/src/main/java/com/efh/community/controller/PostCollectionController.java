package com.efh.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.community.entity.PostCollection;
import com.efh.community.service.PostCollectionService;
import com.efh.community.vo.PostCollectionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/collection")
public class PostCollectionController {
    
    @Autowired
    private PostCollectionService postCollectionService;
    
    /**
     * 收藏或取消收藏
     */
    @PostMapping("/{postId}")
    public Result<?> toggleCollection(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                      @PathVariable Long postId) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        postCollectionService.toggleCollection(userId, postId);
        return Result.success(null);
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{postId}")
    public Result<Boolean> isCollected(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @PathVariable Long postId) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.success(false);
        }
        
        boolean collected = postCollectionService.isCollected(userId, postId);
        return Result.success(collected);
    }
    
    /**
     * 获取我的收藏
     */
    @GetMapping("/my")
    public Result<IPage<PostCollectionVO>> getMyCollections(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        IPage<PostCollectionVO> collections = postCollectionService.getUserCollectionDetails(userId, new Page<>(page, size));
        return Result.success(collections);
    }
}
