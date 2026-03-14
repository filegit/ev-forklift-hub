package com.efh.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.community.entity.Comment;
import com.efh.community.entity.CommentLike;
import com.efh.community.mapper.CommentLikeMapper;
import com.efh.community.mapper.CommentMapper;
import com.efh.community.service.CommentLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论点赞服务实现
 */
@Slf4j
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, Long commentId) {
        log.info("切换评论点赞状态: userId={}, commentId={}", userId, commentId);
        
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getUserId, userId)
               .eq(CommentLike::getCommentId, commentId);
        
        CommentLike like = this.getOne(wrapper);
        
        if (like == null) {
            // 未点赞，创建点赞记录
            like = new CommentLike();
            like.setUserId(userId);
            like.setCommentId(commentId);
            like.setStatus(1);
            this.save(like);
            
            // 更新评论点赞数
            Comment comment = commentMapper.selectById(commentId);
            if (comment != null) {
                comment.setLikeCount(comment.getLikeCount() + 1);
                commentMapper.updateById(comment);
            }
            
            log.info("评论点赞成功: userId={}, commentId={}", userId, commentId);
        } else if (like.getStatus() == 1) {
            // 已点赞，取消点赞
            like.setStatus(0);
            this.updateById(like);
            
            // 更新评论点赞数
            Comment comment = commentMapper.selectById(commentId);
            if (comment != null) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                commentMapper.updateById(comment);
            }
            
            log.info("取消评论点赞: userId={}, commentId={}", userId, commentId);
        } else {
            // 已取消，重新点赞
            like.setStatus(1);
            this.updateById(like);
            
            // 更新评论点赞数
            Comment comment = commentMapper.selectById(commentId);
            if (comment != null) {
                comment.setLikeCount(comment.getLikeCount() + 1);
                commentMapper.updateById(comment);
            }
            
            log.info("重新评论点赞: userId={}, commentId={}", userId, commentId);
        }
    }
    
    @Override
    public boolean isLiked(Long userId, Long commentId) {
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getUserId, userId)
               .eq(CommentLike::getCommentId, commentId)
               .eq(CommentLike::getStatus, 1);
        
        return this.getOne(wrapper) != null;
    }
}
