package com.efh.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efh.community.entity.PostCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子收藏 Mapper
 */
@Mapper
public interface PostCollectionMapper extends BaseMapper<PostCollection> {
}
