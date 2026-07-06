package com.efh.agent.model;

import lombok.Data;

@Data
public class RagChunk {
    private String type;
    private Long id;
    private String title;
    private String content;
    private boolean unlocked;
    private double score;
}
