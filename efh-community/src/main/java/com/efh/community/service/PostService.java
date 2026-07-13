package com.efh.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.community.entity.Post;
import com.efh.community.vo.PostVO;

public interface PostService extends IService<Post> {
    
    /**
     * 创建帖子
     */
    void createPost(Long userId, PostVO postVO);
    
    /**
     * 帖子列表
     */
    IPage<Post> getPostList(Page<Post> page, Integer category, String categoryGroup);
    
    /**
     * 帖子详情
     */
    Post getPostDetail(Long id);
    
    /**
     * 点赞帖子
     */
    void likePost(Long userId, Long postId);

    /**
     * 我的帖子
     */
    IPage<Post> getMyPosts(Long userId, Page<Post> page);

    /**
     * 设置或取消置顶
     */
    void setTop(Long userId, Long postId, Boolean top);
}
