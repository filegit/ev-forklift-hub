package com.efh.parts.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.parts.entity.PartsOrderItem;
import com.efh.parts.mapper.PartsOrderItemMapper;
import com.efh.parts.service.PartsOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class PartsOrderItemServiceImpl extends ServiceImpl<PartsOrderItemMapper, PartsOrderItem> implements PartsOrderItemService {
}
