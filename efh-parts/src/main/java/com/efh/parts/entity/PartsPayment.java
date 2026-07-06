package com.efh.parts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parts_payment")
public class PartsPayment extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private String payNo;
    private String payChannel;
    private BigDecimal amount;
    /** 0待支付 1成功 2失败 */
    private Integer status;
    private LocalDateTime payTime;
    /** 第三方支付流水号（支付宝 trade_no） */
    private String thirdTradeNo;
}
