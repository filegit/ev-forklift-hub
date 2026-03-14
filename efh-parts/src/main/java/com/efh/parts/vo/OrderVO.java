package com.efh.parts.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderVO {
    
    @NotNull(message = "零部件ID不能为空")
    private Long partsId;
    
    @NotNull(message = "数量不能为空")
    private Integer quantity;
    
    @NotBlank(message = "收货地址不能为空")
    private String address;
    
    @NotBlank(message = "联系电话不能为空")
    private String phone;
}
