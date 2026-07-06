package com.efh.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.community.entity.Post;
import com.efh.community.entity.PostLike;
import com.efh.community.mapper.PostLikeMapper;
import com.efh.community.mapper.PostMapper;
import com.efh.community.service.PostLikeService;
import com.efh.community.service.PointsRewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子点赞服务实现
 */
@Slf4j
@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {
    
    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PointsRewardService pointsRewardService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, Long postId) {
        log.info("切换点赞状态: userId={}, postId={}", userId, postId);
        
        // 查询是否已点赞
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
               .eq(PostLike::getPostId, postId);
        
        PostLike like = this.getOne(wrapper);
        
        if (like == null) {
            // 未点赞，创建点赞记录
            like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            like.setStatus(1);
            this.save(like);
            
            // 更新帖子点赞数
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() + 1);
                postMapper.updateById(post);
                rewardPostAuthor(post, userId);
            }
            
            log.info("点赞成功: userId={}, postId={}", userId, postId);
        } else if (like.getStatus() == 1) {
            // 已点赞，取消点赞
            like.setStatus(0);
            this.updateById(like);
            
            // 更新帖子点赞数
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                postMapper.updateById(post);
            }
            
            log.info("取消点赞: userId={}, postId={}", userId, postId);
        } else {
            // 已取消，重新点赞
            like.setStatus(1);
            this.updateById(like);
            
            // 更新帖子点赞数
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() + 1);
                postMapper.updateById(post);
                rewardPostAuthor(post, userId);
            }
            
            log.info("重新点赞: userId={}, postId={}", userId, postId);
        }
    }
    
    @Override
    public boolean isLiked(Long userId, Long postId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
               .eq(PostLike::getPostId, postId)
               .eq(PostLike::getStatus, 1);
        
        return this.getOne(wrapper) != null;
    }

    private void rewardPostAuthor(Post post, Long likerId) {
        if (post.getUserId() != null && !post.getUserId().equals(likerId)) {
            pointsRewardService.reward(post.getUserId(), PointsRewardService.POST_RECEIVED_LIKE, "帖子被点赞");
        }
    }
}
