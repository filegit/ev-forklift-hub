package com.efh.parts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parts_shipment_trace")
public class PartsShipmentTrace extends BaseEntity {

    private Long shipmentId;
    private LocalDateTime traceTime;
    private String location;
    private String description;
}
