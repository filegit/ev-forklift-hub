package com.efh.parts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.parts.entity.PartsShipment;
import com.efh.parts.entity.PartsShipmentTrace;

import java.util.List;

public interface ShipmentService extends IService<PartsShipment> {

    void shipOrder(Long orderId);

    PartsShipment getByOrderId(Long orderId);

    List<PartsShipmentTrace> listTraces(Long shipmentId);

    void markReceived(Long orderId);
}
