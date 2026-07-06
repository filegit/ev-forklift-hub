package com.efh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.service.entity.ServiceOrder;
import com.efh.service.vo.ServiceOrderVO;

import java.util.List;

public interface ServiceOrderService extends IService<ServiceOrder> {
    
    void createServiceOrder(Long userId, ServiceOrderVO serviceOrderVO);

    List<ServiceOrder> listUserOrders(Long userId);

    ServiceOrder getUserOrder(Long userId, Long orderId);

    void cancelOrder(Long userId, Long orderId);
    
    void acceptOrder(Long technicianId, Long orderId);
    
    void completeOrder(Long technicianId, Long orderId);
}
