package com.efh.parts.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderPreviewVO {

    private List<CartItemVO> items;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
}
