package com.efh.agent.service;

import com.efh.agent.model.RagChunk;
import com.efh.agent.vo.ChatRequestVO;
import com.efh.agent.vo.ChatResponseVO;
import com.efh.agent.vo.ChatSourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentChatService {

    @Autowired
    private RagRetrievalService ragRetrievalService;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private DocumentTextService documentTextService;

    public ChatResponseVO chat(Long userId, ChatRequestVO request) {
        String scope = request.getScope() == null ? "all" : request.getScope();
        List<RagChunk> chunks = ragRetrievalService.retrieve(request.getQuestion(), userId, scope);
        LlmClient.ChatResult chatResult = llmClient.generateAnswer(request.getQuestion(), chunks);

        ChatResponseVO response = new ChatResponseVO();
        response.setAnswer(chatResult.getAnswer());
        response.setLlmUsed(chatResult.isLlmUsed());
        for (RagChunk chunk : chunks) {
            ChatSourceVO source = new ChatSourceVO();
            source.setType(chunk.getType());
            source.setId(chunk.getId());
            source.setTitle(chunk.getTitle());
            source.setSnippet(documentTextService.truncate(chunk.getContent(), 160));
            source.setUnlocked(chunk.isUnlocked());
            source.setLink(buildLink(chunk));
            response.getSources().add(source);
        }
        return response;
    }

    private String buildLink(RagChunk chunk) {
        if ("knowledge".equals(chunk.getType())) {
            return "/knowledge/" + chunk.getId();
        }
        if ("post".equals(chunk.getType())) {
            return "/post/" + chunk.getId();
        }
        return "/";
    }
}
