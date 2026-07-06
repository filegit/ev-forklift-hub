package com.efh.parts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parts_order")
public class PartsOrder extends BaseEntity {

    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    /** 0待付款 1待发货 2待收货 3已完成 4已取消 */
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime cancelTime;
}
