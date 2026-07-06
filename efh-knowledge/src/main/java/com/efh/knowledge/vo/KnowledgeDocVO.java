package com.efh.knowledge.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class KnowledgeDocVO {
    private Long id;
    private String title;
    private String summary;
    private String category;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Integer accessType;
    private Integer pointsPrice;
    private BigDecimal moneyPrice;
    private Integer status;
    private Integer downloadCount;
    private Integer viewCount;
    private LocalDateTime createTime;
    private Boolean unlocked;
    private String accessLabel;
}
