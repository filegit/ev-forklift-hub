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
}
