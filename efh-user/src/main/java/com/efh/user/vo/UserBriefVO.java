package com.efh.user.vo;

import lombok.Data;

@Data
public class UserBriefVO {
    private Long id;
    private String username;
    private String nickname;
    private Integer userType;
    private Integer availablePoints;
}
