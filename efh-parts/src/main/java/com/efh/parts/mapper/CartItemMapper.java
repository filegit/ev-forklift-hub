package com.efh.parts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efh.parts.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
