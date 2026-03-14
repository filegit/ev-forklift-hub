package com.efh.parts.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PartsVO {
    
    @NotBlank(message = "名称不能为空")
    private String name;
    
    private String description;
    
    @NotBlank(message = "分类不能为空")
    private String category;
    
    private String brand;
    private String model;
    
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    
    @NotNull(message = "库存不能为空")
    private Integer stock;
    
    private String images;
}
