package com.efh.user.vo;

import lombok.Data;

@Data
public class PointsPayStatusVO {
    private String payNo;
    private Integer packageId;
    private Integer points;
    private Integer status;
    private String payChannel;
    private String thirdTradeNo;
}
