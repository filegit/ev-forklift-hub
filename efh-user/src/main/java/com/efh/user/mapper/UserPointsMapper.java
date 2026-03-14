package com.efh.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efh.user.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分 Mapper
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
}
