package com.efh.knowledge.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DocSaveVO {
    private String title;
    private String summary;
    private String category;
    private Integer accessType;
    private Integer pointsPrice;
    private BigDecimal moneyPrice;
}
