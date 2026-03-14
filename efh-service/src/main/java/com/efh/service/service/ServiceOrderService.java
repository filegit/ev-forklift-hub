package com.efh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.service.entity.ServiceOrder;
import com.efh.service.vo.ServiceOrderVO;

public interface ServiceOrderService extends IService<ServiceOrder> {
    
    /**
     * 创建服务工单
     */
    void createServiceOrder(Long userId, ServiceOrderVO serviceOrderVO);
    
    /**
     * 技师接单
     */
    void acceptOrder(Long technicianId, Long orderId);
    
    /**
     * 完成工单
     */
    void completeOrder(Long technicianId, Long orderId);
}
