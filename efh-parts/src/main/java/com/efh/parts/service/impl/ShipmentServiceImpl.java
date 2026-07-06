package com.efh.parts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.entity.PartsShipment;
import com.efh.parts.entity.PartsShipmentTrace;
import com.efh.parts.mapper.PartsShipmentMapper;
import com.efh.parts.mapper.PartsShipmentTraceMapper;
import com.efh.parts.service.PartsOrderService;
import com.efh.parts.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShipmentServiceImpl extends ServiceImpl<PartsShipmentMapper, PartsShipment> implements ShipmentService {

    @Autowired
    @Lazy
    private PartsOrderService partsOrderService;

    @Autowired
    private PartsShipmentTraceMapper traceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartsShipment shipOrder(Long operatorId, Long orderId, String carrier, String trackingNo, String firstLocation) {
        PartsOrder order = partsOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!canOperateShipment(operatorId, order)) {
            throw new BusinessException("无权发货该订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("当前订单不是待发货状态");
        }
        if (isBlank(carrier) || isBlank(trackingNo)) {
            throw new BusinessException("请填写承运商和运单号");
        }
        if (getByOrderId(orderId) != null) {
            throw new BusinessException("该订单已发货");
        }

        PartsShipment shipment = new PartsShipment();
        shipment.setOrderId(orderId);
        shipment.setOrderNo(order.getOrderNo());
        shipment.setCarrier(carrier.trim());
        shipment.setTrackingNo(trackingNo.trim());
        shipment.setStatus(1);
        shipment.setShipTime(LocalDateTime.now());
        save(shipment);

        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        partsOrderService.updateById(order);

        saveTrace(shipment.getId(), LocalDateTime.now(), defaultLocation(firstLocation, "备件仓库"),
                carrier.trim() + "已揽收，运单号：" + trackingNo.trim());
        return shipment;
    }

    @Override
    public PartsShipment getByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<PartsShipment>()
                .eq(PartsShipment::getOrderId, orderId)
                .orderByDesc(PartsShipment::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public List<PartsShipmentTrace> listTraces(Long shipmentId) {
        return traceMapper.selectList(new LambdaQueryWrapper<PartsShipmentTrace>()
                .eq(PartsShipmentTrace::getShipmentId, shipmentId)
                .orderByDesc(PartsShipmentTrace::getTraceTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appendTrace(Long operatorId, Long orderId, String location, String description) {
        PartsOrder order = partsOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!canOperateShipment(operatorId, order)) {
            throw new BusinessException("无权更新该订单物流");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("当前订单不是运输中状态");
        }
        PartsShipment shipment = getByOrderId(orderId);
        if (shipment == null) {
            throw new BusinessException("请先录入发货信息");
        }
        if (isBlank(description)) {
            throw new BusinessException("请填写物流轨迹说明");
        }
        saveTrace(shipment.getId(), LocalDateTime.now(), defaultLocation(location, "物流节点"), description.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReceived(Long orderId) {
        PartsShipment shipment = getByOrderId(orderId);
        if (shipment != null) {
            shipment.setStatus(3);
            shipment.setReceiveTime(LocalDateTime.now());
            updateById(shipment);
            saveTrace(shipment.getId(), LocalDateTime.now(), "已签收", "货物已签收，订单履约完成");
        }
    }

    private void saveTrace(Long shipmentId, LocalDateTime time, String location, String desc) {
        PartsShipmentTrace trace = new PartsShipmentTrace();
        trace.setShipmentId(shipmentId);
        trace.setTraceTime(time);
        trace.setLocation(location);
        trace.setDescription(desc);
        traceMapper.insert(trace);
    }

    private String defaultLocation(String location, String fallback) {
        return isBlank(location) ? fallback : location.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean canOperateShipment(Long operatorId, PartsOrder order) {
        return order.getSellerId().equals(operatorId) || Long.valueOf(1L).equals(operatorId);
    }
}
