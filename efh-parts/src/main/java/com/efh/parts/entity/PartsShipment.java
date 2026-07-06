package com.efh.parts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parts_shipment")
public class PartsShipment extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private String carrier;
    private String trackingNo;
    /** 0待揽收 1运输中 2派送中 3已签收 */
    private Integer status;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
}
