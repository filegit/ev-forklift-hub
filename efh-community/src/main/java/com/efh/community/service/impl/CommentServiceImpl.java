package com.efh.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.community.entity.Comment;
import com.efh.community.entity.Post;
import com.efh.community.mapper.CommentMapper;
import com.efh.community.service.CommentService;
import com.efh.community.service.PostService;
import com.efh.community.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Autowired
    private PostService postService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(Long userId, CommentVO commentVO) {
        log.info("发表评论: userId={}, postId={}, content={}", userId, commentVO.getPostId(), commentVO.getContent());
        
        // 1. 验证帖子是否存在
        Post post = postService.getById(commentVO.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        if (post.getStatus() != 1) {
            throw new BusinessException("帖子已被删除或未发布");
        }
        
        // 2. 如果是回复评论，验证父评论是否存在
        if (commentVO.getParentId() != null && commentVO.getParentId() > 0) {
            Comment parentComment = this.getById(commentVO.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
        }
        
        // 3. 创建评论
        Comment comment = new Comment();
        comment.setPostId(commentVO.getPostId());
        comment.setUserId(userId);
        comment.setContent(commentVO.getContent());
        comment.setParentId(commentVO.getParentId() != null ? commentVO.getParentId() : 0L);
        comment.setLikeCount(0);
        
        boolean saved = this.save(comment);
        if (!saved) {
            throw new BusinessException("发表评论失败");
        }
        
        // 4. 更新帖子的评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postService.updateById(post);
        
        log.info("发表评论成功: commentId={}", comment.getId());
    }
    
    @Override
    public IPage<Comment> getCommentList(Long postId, Page<Comment> page) {
        log.info("查询评论列表: postId={}, page={}, size={}", postId, page.getCurrent(), page.getSize());
        
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId);
        wrapper.orderByDesc(Comment::getCreateTime);
        
        IPage<Comment> result = this.page(page, wrapper);
        log.info("查询评论列表成功: total={}", result.getTotal());
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        log.info("删除评论: userId={}, commentId={}", userId, commentId);
        
        // 1. 查询评论
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 2. 验证是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }
        
        // 3. 删除评论
        boolean deleted = this.removeById(commentId);
        if (!deleted) {
            throw new BusinessException("删除评论失败");
        }
        
        // 4. 更新帖子的评论数
        Post post = postService.getById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postService.updateById(post);
        }
        
        log.info("删除评论成功: commentId={}", commentId);
    }
}
