package com.efh.service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.efh.service.entity.ServiceOrder;
import com.efh.service.mapper.ServiceOrderMapper;
import com.efh.service.service.ServiceOrderService;
import com.efh.service.vo.ServiceOrderVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceOrderServiceImpl extends ServiceImpl<ServiceOrderMapper, ServiceOrder> implements ServiceOrderService {
    
    @Override
    public void createServiceOrder(Long userId, ServiceOrderVO serviceOrderVO) {
        ServiceOrder order = new ServiceOrder();
        order.setUserId(userId);
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setServiceType(serviceOrderVO.getServiceType());
        order.setTitle(serviceOrderVO.getTitle() != null && !serviceOrderVO.getTitle().isEmpty()
                ? serviceOrderVO.getTitle()
                : resolveTitle(serviceOrderVO.getServiceType()));
        order.setDescription(serviceOrderVO.getDescription());
        order.setImages(serviceOrderVO.getImages());
        order.setAddress(serviceOrderVO.getAddress());
        order.setPhone(serviceOrderVO.getPhone());
        order.setStatus(0);
        
        this.save(order);
    }

    @Override
    public List<ServiceOrder> listUserOrders(Long userId) {
        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrder::getUserId, userId).orderByDesc(ServiceOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public ServiceOrder getUserOrder(Long userId, Long orderId) {
        ServiceOrder order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("工单不存在");
        }
        return order;
    }

    @Override
    public void cancelOrder(Long userId, Long orderId) {
        ServiceOrder order = getUserOrder(userId, orderId);
        if (order.getStatus() != 0) {
            throw new BusinessException("当前状态不可取消");
        }
        order.setStatus(4);
        this.updateById(order);
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
        order.setStatus(2);
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

    private String resolveTitle(Integer serviceType) {
        if (serviceType == null) {
            return "维修服务";
        }
        switch (serviceType) {
            case 1: return "故障诊断";
            case 2: return "维修保养";
            case 3: return "紧急救援";
            default: return "维修服务";
        }
    }
}
