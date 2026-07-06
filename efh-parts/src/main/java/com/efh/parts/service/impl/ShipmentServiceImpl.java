package com.efh.parts.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.entity.PartsShipment;
import com.efh.parts.entity.PartsShipmentTrace;
import com.efh.parts.mapper.PartsShipmentMapper;
import com.efh.parts.service.PartsOrderService;
import com.efh.parts.service.ShipmentService;
import com.efh.parts.mapper.PartsShipmentTraceMapper;
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
    public void shipOrder(Long orderId) {
        PartsOrder order = partsOrderService.getById(orderId);
        if (order == null || order.getStatus() != 1) {
            return;
        }

        PartsShipment existing = getByOrderId(orderId);
        if (existing != null) {
            return;
        }

        String trackingNo = "SF" + RandomUtil.randomNumbers(12);
        PartsShipment shipment = new PartsShipment();
        shipment.setOrderId(orderId);
        shipment.setOrderNo(order.getOrderNo());
        shipment.setCarrier("顺丰速运");
        shipment.setTrackingNo(trackingNo);
        shipment.setStatus(1);
        shipment.setShipTime(LocalDateTime.now());
        save(shipment);

        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        partsOrderService.updateById(order);

        LocalDateTime now = LocalDateTime.now();
        saveTrace(shipment.getId(), now.minusHours(2), "上海", "快件已从【上海仓库】发出");
        saveTrace(shipment.getId(), now.minusHours(1), "上海转运中心", "快件到达【上海转运中心】");
        saveTrace(shipment.getId(), now, order.getReceiverAddress(), "快件正在派送中，请保持电话畅通");
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

    @Transactional(rollbackFor = Exception.class)
    public void markReceived(Long orderId) {
        PartsShipment shipment = getByOrderId(orderId);
        if (shipment != null) {
            shipment.setStatus(3);
            shipment.setReceiveTime(LocalDateTime.now());
            updateById(shipment);
            saveTrace(shipment.getId(), LocalDateTime.now(), "已签收", "快件已签收，感谢使用顺丰速运");
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
}
