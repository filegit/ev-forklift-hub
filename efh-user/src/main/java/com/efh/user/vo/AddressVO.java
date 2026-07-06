package com.efh.user.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddressVO {

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String province;
    private String city;
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    private Integer isDefault;
}
