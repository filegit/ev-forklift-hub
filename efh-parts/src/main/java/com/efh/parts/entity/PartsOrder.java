package com.efh.parts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parts_order")
public class PartsOrder extends BaseEntity {
    
    private String orderNo; // 订单号
    private Long buyerId; // 买家ID
    private Long sellerId; // 卖家ID
    private Long partsId; // 零部件ID
    private String partsName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status; // 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消
    private String address; // 收货地址
    private String phone; // 联系电话
}
