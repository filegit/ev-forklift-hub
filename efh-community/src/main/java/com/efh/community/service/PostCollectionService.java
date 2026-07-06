package com.efh.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.community.entity.PostCollection;
import com.efh.community.vo.PostCollectionVO;

/**
 * 帖子收藏服务接口
 */
public interface PostCollectionService extends IService<PostCollection> {
    
    /**
     * 收藏或取消收藏
     */
    void toggleCollection(Long userId, Long postId);
    
    /**
     * 检查是否已收藏
     */
    boolean isCollected(Long userId, Long postId);
    
    /**
     * 获取用户收藏列表
     */
    IPage<PostCollection> getUserCollections(Long userId, Page<PostCollection> page);

    /**
     * 获取用户收藏列表（含帖子信息）
     */
    IPage<PostCollectionVO> getUserCollectionDetails(Long userId, Page<PostCollection> page);
}
