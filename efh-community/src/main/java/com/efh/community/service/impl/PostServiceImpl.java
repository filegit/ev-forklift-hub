package com.efh.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.community.entity.Post;
import com.efh.community.mapper.PostMapper;
import com.efh.community.service.PostService;
import com.efh.community.service.PointsRewardService;
import com.efh.community.vo.PostVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PointsRewardService pointsRewardService;
    
    private static final String POST_CACHE_PREFIX = "post:detail:";
    private static final int CACHE_EXPIRE_MINUTES = 30;
    
    @Override
    public void createPost(Long userId, PostVO postVO) {
        log.info("创建帖子开始: userId={}, title={}", userId, postVO.getTitle());
        
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(postVO.getTitle());
        post.setContent(postVO.getContent());
        post.setCategory(postVO.getCategory());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        post.setStatus(1);  // 直接发布
        
        boolean saved = this.save(post);
        if (!saved) {
            throw new BusinessException("发帖失败");
        }
        
        log.info("创建帖子成功: postId={}", post.getId());
        pointsRewardService.reward(userId, PointsRewardService.POST_CREATE, "发布帖子");
    }
    
    @Override
    public IPage<Post> getPostList(Page<Post> page, Integer category, String categoryGroup) {
        log.info("查询帖子列表: page={}, size={}, category={}, categoryGroup={}", page.getCurrent(), page.getSize(), category, categoryGroup);
        
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);  // 只查询已发布的帖子
        
        if ("technical".equalsIgnoreCase(categoryGroup)) {
            wrapper.in(Post::getCategory, 1, 5, 6, 7);
        } else if (category != null && category > 0) {
            wrapper.eq(Post::getCategory, category);
        }
        
        wrapper.orderByDesc(Post::getIsTop)
               .orderByDesc(Post::getTopTime)
               .orderByDesc(Post::getCreateTime);
        
        IPage<Post> result = this.page(page, wrapper);
        log.info("查询帖子列表成功: total={}", result.getTotal());
        
        return result;
    }
    
    @Override
    public Post getPostDetail(Long id) {
        log.info("查询帖子详情: postId={}", id);
        
        // 先从缓存获取
        String cacheKey = POST_CACHE_PREFIX + id;
        Post cachedPost = (Post) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedPost != null) {
            log.info("从缓存获取帖子详情: postId={}", id);
            // 增加浏览量（异步更新数据库）
            incrementViewCount(id);
            return cachedPost;
        }
        
        // 缓存未命中，从数据库查询
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        if (post.getStatus() != 1) {
            throw new BusinessException("帖子已被删除或未发布");
        }
        
        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        this.updateById(post);
        
        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, post, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        log.info("查询帖子详情成功: postId={}, viewCount={}", id, post.getViewCount());
        return post;
    }
    
    @Override
    public void likePost(Long userId, Long postId) {
        log.info("点赞帖子: userId={}, postId={}", userId, postId);
        
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        if (post.getStatus() != 1) {
            throw new BusinessException("帖子已被删除或未发布");
        }
        
        // 增加点赞数
        post.setLikeCount(post.getLikeCount() + 1);
        this.updateById(post);
        
        // 清除缓存
        String cacheKey = POST_CACHE_PREFIX + postId;
        redisTemplate.delete(cacheKey);
        
        log.info("点赞帖子成功: postId={}, likeCount={}", postId, post.getLikeCount());
    }

    @Override
    public IPage<Post> getMyPosts(Long userId, Page<Post> page) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
               .eq(Post::getStatus, 1)
               .orderByDesc(Post::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void setTop(Long userId, Long postId, Boolean top) {
        if (userId == null) {
            throw new BusinessException("未授权");
        }
        Post post = this.getById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("帖子不存在或不可操作");
        }
        post.setIsTop(Boolean.TRUE.equals(top) ? 1 : 0);
        post.setTopTime(Boolean.TRUE.equals(top) ? LocalDateTime.now() : null);
        this.updateById(post);
        redisTemplate.delete(POST_CACHE_PREFIX + postId);
    }
    
    /**
     * 异步增加浏览量
     */
    private void incrementViewCount(Long postId) {
        try {
            Post post = this.getById(postId);
            if (post != null) {
                post.setViewCount(post.getViewCount() + 1);
                this.updateById(post);
            }
        } catch (Exception e) {
            log.error("增加浏览量失败: postId={}", postId, e);
        }
    }
}
