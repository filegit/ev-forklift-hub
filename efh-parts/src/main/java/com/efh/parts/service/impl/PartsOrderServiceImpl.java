package com.efh.parts.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.parts.entity.Parts;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.mapper.PartsOrderMapper;
import com.efh.parts.service.PartsOrderService;
import com.efh.parts.service.PartsService;
import com.efh.parts.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PartsOrderServiceImpl extends ServiceImpl<PartsOrderMapper, PartsOrder> implements PartsOrderService {
    
    @Autowired
    private PartsService partsService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Long buyerId, OrderVO orderVO) {
        // 查询零部件
        Parts parts = partsService.getById(orderVO.getPartsId());
        if (parts == null) {
            throw new BusinessException("零部件不存在");
        }
        
        if (parts.getStatus() == 0) {
            throw new BusinessException("零部件已下架");
        }
        
        if (parts.getStock() < orderVO.getQuantity()) {
            throw new BusinessException("库存不足");
        }
        
        // 创建订单
        PartsOrder order = new PartsOrder();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setBuyerId(buyerId);
        order.setSellerId(parts.getSellerId());
        order.setPartsId(parts.getId());
        order.setPartsName(parts.getName());
        order.setPrice(parts.getPrice());
        order.setQuantity(orderVO.getQuantity());
        order.setTotalAmount(parts.getPrice().multiply(new BigDecimal(orderVO.getQuantity())));
        order.setStatus(0);
        order.setAddress(orderVO.getAddress());
        order.setPhone(orderVO.getPhone());
        
        this.save(order);
        
        // 减少库存
        parts.setStock(parts.getStock() - orderVO.getQuantity());
        partsService.updateById(parts);
    }
    
    @Override
    public void cancelOrder(Long userId, Long orderId) {
        PartsOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许取消");
        }
        
        order.setStatus(4);
        this.updateById(order);
        
        // 恢复库存
        Parts parts = partsService.getById(order.getPartsId());
        parts.setStock(parts.getStock() + order.getQuantity());
        partsService.updateById(parts);
    }
}
