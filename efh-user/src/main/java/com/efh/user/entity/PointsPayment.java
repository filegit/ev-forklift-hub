package com.efh.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_payment")
public class PointsPayment extends BaseEntity {

    private Long userId;
    private String payNo;
    private Integer packageId;
    private Integer points;
    private BigDecimal amount;
    private String payChannel;
    private Integer status;
    private LocalDateTime payTime;
    private String thirdTradeNo;
}
