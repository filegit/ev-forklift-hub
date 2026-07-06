package com.efh.service.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ServiceOrderVO {
    
    @NotNull(message = "服务类型不能为空")
    private Integer serviceType;
    
    private String title;
    
    @NotBlank(message = "描述不能为空")
    private String description;
    
    private String images;
    
    @NotBlank(message = "服务地址不能为空")
    private String address;
    
    @NotBlank(message = "联系电话不能为空")
    private String phone;
}
