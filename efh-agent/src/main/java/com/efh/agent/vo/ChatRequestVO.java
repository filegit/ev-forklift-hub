package com.efh.agent.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRequestVO {
    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题最大500字")
    private String question;

    /** all | knowledge | community */
    private String scope = "all";

    /** Client sends this back to keep multi-turn memory. */
    private String sessionId;

    /** Optional flag; streaming usually uses /chat/stream directly. */
    private Boolean stream = false;

    /** Image URLs for multimodal diagnosis entry. */
    private List<String> imageUrls = new ArrayList<>();

    /** Whether the Agent may create a service ticket after user confirmation. */
    private Boolean allowCreateTicket = false;
}
