package com.efh.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 积分兑换记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_exchange")
public class PointsExchange extends BaseEntity {
    
    private Long userId;        // 用户ID
    private String itemName;    // 兑换物品名称
    private Integer points;     // 消耗积分
    private String status;      // 状态：pending-待发货，completed-已完成，cancelled-已取消
}
