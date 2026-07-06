package com.efh.agent.service.tool;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册表（问题3）：统一管理可被 LLM Function Call 调用的工具
 */
@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new HashMap<>();

    public ToolRegistry(List<AgentTool> toolList) {
        for (AgentTool t : toolList) {
            tools.put(t.name(), t);
        }
    }

    public AgentTool get(String name) {
        return tools.get(name);
    }

    public Map<String, AgentTool> all() {
        return tools;
    }

    /** 转为 OpenAI tools 格式 */
    public List<Map<String, Object>> toOpenAiTools() {
        return tools.values().stream().map(t -> {
            Map<String, Object> fn = new HashMap<>();
            fn.put("name", t.name());
            fn.put("description", t.description());
            Map<String, Object> params = new HashMap<>();
            params.put("type", "object");
            Map<String, Object> props = new HashMap<>();
            props.put("query", mapOf("type", "string", "description", "搜索关键词"));
            params.put("properties", props);
            fn.put("parameters", params);
            Map<String, Object> tool = new HashMap<>();
            tool.put("type", "function");
            tool.put("function", fn);
            return tool;
        }).collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }
}
