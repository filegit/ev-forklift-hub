package com.efh.parts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.parts.entity.PartsShipment;
import com.efh.parts.entity.PartsShipmentTrace;

import java.util.List;

public interface ShipmentService extends IService<PartsShipment> {

    PartsShipment shipOrder(Long operatorId, Long orderId, String carrier, String trackingNo, String firstLocation);

    PartsShipment getByOrderId(Long orderId);

    List<PartsShipmentTrace> listTraces(Long shipmentId);

    void appendTrace(Long operatorId, Long orderId, String location, String description);

    void markReceived(Long orderId);
}
