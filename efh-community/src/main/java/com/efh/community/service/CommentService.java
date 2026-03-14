package com.efh.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.community.entity.Comment;
import com.efh.community.vo.CommentVO;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * 发表评论
     */
    void createComment(Long userId, CommentVO commentVO);
    
    /**
     * 获取帖子的评论列表
     */
    IPage<Comment> getCommentList(Long postId, Page<Comment> page);
    
    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);
}
