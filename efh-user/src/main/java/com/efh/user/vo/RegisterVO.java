package com.efh.user.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterVO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    
    private String phone;
}
