package com.efh.parts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parts_order_item")
public class PartsOrderItem extends BaseEntity {

    private Long orderId;
    private Long partsId;
    private String partsName;
    private String partsImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
