package com.efh.parts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.vo.OrderVO;

public interface PartsOrderService extends IService<PartsOrder> {
    
    /**
     * 创建订单
     */
    void createOrder(Long buyerId, OrderVO orderVO);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long userId, Long orderId);
}
