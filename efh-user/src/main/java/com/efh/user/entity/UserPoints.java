package com.efh.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户积分实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_points")
public class UserPoints extends BaseEntity {
    
    private Long userId;           // 用户ID
    private Integer totalPoints;   // 总积分
    private Integer usedPoints;    // 已使用积分
    private Integer availablePoints; // 可用积分
}
