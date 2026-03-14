package com.efh.service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.service.entity.ServiceOrder;
import com.efh.service.mapper.ServiceOrderMapper;
import com.efh.service.service.ServiceOrderService;
import com.efh.service.vo.ServiceOrderVO;
import org.springframework.stereotype.Service;

@Service
public class ServiceOrderServiceImpl extends ServiceImpl<ServiceOrderMapper, ServiceOrder> implements ServiceOrderService {
    
    @Override
    public void createServiceOrder(Long userId, ServiceOrderVO serviceOrderVO) {
        ServiceOrder order = new ServiceOrder();
        order.setUserId(userId);
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setServiceType(serviceOrderVO.getServiceType());
        order.setTitle(serviceOrderVO.getTitle());
        order.setDescription(serviceOrderVO.getDescription());
        order.setImages(serviceOrderVO.getImages());
        order.setAddress(serviceOrderVO.getAddress());
        order.setPhone(serviceOrderVO.getPhone());
        order.setStatus(0);
        
        this.save(order);
    }
    
    @Override
    public void acceptOrder(Long technicianId, Long orderId) {
        ServiceOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("工单状态不允许接单");
        }
        
        order.setTechnicianId(technicianId);
        order.setStatus(1);
        this.updateById(order);
    }
    
    @Override
    public void completeOrder(Long technicianId, Long orderId) {
        ServiceOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        
        if (!order.getTechnicianId().equals(technicianId)) {
            throw new BusinessException("无权操作");
        }
        
        if (order.getStatus() != 2) {
            throw new BusinessException("工单状态不允许完成");
        }
        
        order.setStatus(3);
        this.updateById(order);
    }
}
