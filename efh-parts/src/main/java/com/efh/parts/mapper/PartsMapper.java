package com.efh.parts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efh.parts.entity.Parts;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartsMapper extends BaseMapper<Parts> {
}
