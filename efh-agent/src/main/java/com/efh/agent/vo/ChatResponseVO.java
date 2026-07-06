package com.efh.agent.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatResponseVO {
    private String answer;
    private boolean llmUsed;
    private List<ChatSourceVO> sources = new ArrayList<>();
}
