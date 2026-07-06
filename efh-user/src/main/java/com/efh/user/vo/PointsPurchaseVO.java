package com.efh.user.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PointsPurchaseVO {
    /** 套餐ID：1-100积分 2-500积分 3-1000积分 */
    @NotNull(message = "请选择购买套餐")
    private Integer packageId;
}
