package com.efh.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.community.entity.PostLike;

/**
 * 帖子点赞服务接口
 */
public interface PostLikeService extends IService<PostLike> {
    
    /**
     * 点赞或取消点赞
     */
    void toggleLike(Long userId, Long postId);
    
    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long userId, Long postId);
}
