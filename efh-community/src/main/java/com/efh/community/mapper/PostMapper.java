package com.efh.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efh.community.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
