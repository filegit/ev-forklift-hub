package com.efh.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efh.community.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞 Mapper
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}
