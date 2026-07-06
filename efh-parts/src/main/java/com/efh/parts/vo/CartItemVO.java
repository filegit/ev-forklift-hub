package com.efh.parts.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItemVO {

    private Long id;
    private Long partsId;
    private String name;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer status;
    private Long sellerId;
}
