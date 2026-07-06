package com.efh.agent.vo;

import lombok.Data;

@Data
public class ChatSourceVO {
    private String type;
    private Long id;
    private String title;
    private String snippet;
    private boolean unlocked;
    private String link;
}
