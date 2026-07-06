package com.efh.user.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SendLoginSmsVO {

    @NotBlank(message = "用户名不能为空")
    private String username;
}
