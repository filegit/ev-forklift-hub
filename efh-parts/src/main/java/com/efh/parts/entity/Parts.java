package com.efh.parts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 零部件实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parts")
public class Parts extends BaseEntity {
    
    private Long sellerId; // 卖家ID
    private String name;
    private String description;
    private String category; // 分类：电池、电机、控制器等
    private String brand; // 品牌
    private String model; // 型号
    private BigDecimal price;
    private Integer stock; // 库存
    private String images; // 图片，多个用逗号分隔
    private Integer status; // 0-下架 1-上架
    private Integer salesCount; // 销量
}
