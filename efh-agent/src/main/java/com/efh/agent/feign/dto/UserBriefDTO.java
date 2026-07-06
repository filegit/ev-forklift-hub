package com.efh.agent.feign.dto;

import lombok.Data;

@Data
public class UserBriefDTO {
    private Long id;
    private String username;
    private String nickname;
    private Integer userType;
    private Integer availablePoints;
}
