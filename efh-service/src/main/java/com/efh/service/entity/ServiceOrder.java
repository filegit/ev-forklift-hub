package com.efh.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务工单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_order")
public class ServiceOrder extends BaseEntity {
    
    private Long userId; // 用户ID
    private Long technicianId; // 技师ID
    private String orderNo; // 工单号
    private Integer serviceType; // 1-维修 2-保养 3-咨询
    private String title;
    private String description;
    private String images; // 图片
    private String address; // 服务地址
    private String phone; // 联系电话
    private Integer status; // 0-待接单 1-已接单 2-服务中 3-已完成 4-已取消
    private String feedback; // 用户反馈
    private Integer rating; // 评分 1-5
}
