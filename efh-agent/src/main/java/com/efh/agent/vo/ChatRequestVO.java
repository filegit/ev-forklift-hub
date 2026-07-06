package com.efh.agent.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChatRequestVO {
    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题最多500字")
    private String question;

    /** all | knowledge | community */
    private String scope = "all";

    /** 多轮会话ID，同一用户同一 sessionId 在多实例间共享上下文（问题12） */
    private String sessionId;

    /** 是否流式（也可走 /chat/stream 专用接口） */
    private Boolean stream = false;
}
