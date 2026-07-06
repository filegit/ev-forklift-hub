package com.efh.agent.service.tool;

import com.efh.agent.model.RagChunk;
import com.efh.agent.service.RagRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommunitySearchTool implements AgentTool {

    @Autowired
    private RagRetrievalService ragRetrievalService;

    @Override
    public String name() { return "community_search"; }

    @Override
    public String description() { return "搜索社区帖子和评论，参数 query"; }

    @Override
    public boolean requiresAuth() { return false; }

    @Override
    public boolean adminOnly() { return false; }

    @Override
    public String execute(ToolContext ctx, Map<String, Object> args) {
        String query = String.valueOf(args.getOrDefault("query", ""));
        List<RagChunk> chunks = ragRetrievalService.retrieve(query, ctx.getUserId(), "community");
        return chunks.stream().limit(3).map(c -> c.getTitle() + ": " + c.getContent()).collect(Collectors.joining("\n"));
    }
}
