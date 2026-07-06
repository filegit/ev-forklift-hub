package com.efh.parts.vo;

import com.efh.parts.entity.PartsOrder;
import com.efh.parts.entity.PartsOrderItem;
import com.efh.parts.entity.PartsPayment;
import com.efh.parts.entity.PartsShipment;
import com.efh.parts.entity.PartsShipmentTrace;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailVO {

    private PartsOrder order;
    private List<PartsOrderItem> items;
    private PartsPayment payment;
    private PartsShipment shipment;
    private List<PartsShipmentTrace> traces;
}
