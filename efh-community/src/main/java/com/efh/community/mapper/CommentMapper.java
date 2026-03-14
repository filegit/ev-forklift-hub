package com.efh.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efh.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
