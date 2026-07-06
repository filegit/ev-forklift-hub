package com.efh.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.community.entity.Post;
import com.efh.community.entity.PostCollection;
import com.efh.community.mapper.PostCollectionMapper;
import com.efh.community.mapper.PostMapper;
import com.efh.community.service.PostCollectionService;
import com.efh.community.vo.PostCollectionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子收藏服务实现
 */
@Slf4j
@Service
public class PostCollectionServiceImpl extends ServiceImpl<PostCollectionMapper, PostCollection> implements PostCollectionService {

    @Autowired
    private PostMapper postMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleCollection(Long userId, Long postId) {
        log.info("切换收藏状态: userId={}, postId={}", userId, postId);
        
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostCollection::getUserId, userId)
               .eq(PostCollection::getPostId, postId);
        
        PostCollection collection = this.getOne(wrapper);
        
        if (collection == null) {
            // 未收藏，创建收藏记录
            collection = new PostCollection();
            collection.setUserId(userId);
            collection.setPostId(postId);
            collection.setStatus(1);
            this.save(collection);
            log.info("收藏成功: userId={}, postId={}", userId, postId);
        } else if (collection.getStatus() == 1) {
            // 已收藏，取消收藏
            collection.setStatus(0);
            this.updateById(collection);
            log.info("取消收藏: userId={}, postId={}", userId, postId);
        } else {
            // 已取消，重新收藏
            collection.setStatus(1);
            this.updateById(collection);
            log.info("重新收藏: userId={}, postId={}", userId, postId);
        }
    }
    
    @Override
    public boolean isCollected(Long userId, Long postId) {
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostCollection::getUserId, userId)
               .eq(PostCollection::getPostId, postId)
               .eq(PostCollection::getStatus, 1);
        
        return this.getOne(wrapper) != null;
    }
    
    @Override
    public IPage<PostCollection> getUserCollections(Long userId, Page<PostCollection> page) {
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostCollection::getUserId, userId)
               .eq(PostCollection::getStatus, 1)
               .orderByDesc(PostCollection::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public IPage<PostCollectionVO> getUserCollectionDetails(Long userId, Page<PostCollection> page) {
        IPage<PostCollection> collections = getUserCollections(userId, page);
        Page<PostCollectionVO> voPage = new Page<>(collections.getCurrent(), collections.getSize(), collections.getTotal());
        List<PostCollectionVO> records = new ArrayList<>();
        for (PostCollection collection : collections.getRecords()) {
            PostCollectionVO vo = new PostCollectionVO();
            vo.setId(collection.getId());
            vo.setPostId(collection.getPostId());
            vo.setCreateTime(collection.getCreateTime());
            Post post = postMapper.selectById(collection.getPostId());
            if (post != null) {
                vo.setPostTitle(post.getTitle());
                vo.setPostContent(post.getContent());
            } else {
                vo.setPostTitle("帖子已删除");
                vo.setPostContent("");
            }
            records.add(vo);
        }
        voPage.setRecords(records);
        return voPage;
    }
}
