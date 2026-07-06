package com.efh.parts.vo;

import lombok.Data;

@Data
public class PayStatusVO {
    private String payNo;
    private String orderNo;
    private Long orderId;
    /** 0待支付 1成功 2失败 */
    private Integer status;
    private String payChannel;
    private String thirdTradeNo;
}
