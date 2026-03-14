package com.efh.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.community.entity.CommentLike;

/**
 * 评论点赞服务接口
 */
public interface CommentLikeService extends IService<CommentLike> {
    
    /**
     * 点赞或取消点赞
     */
    void toggleLike(Long userId, Long commentId);
    
    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long userId, Long commentId);
}
