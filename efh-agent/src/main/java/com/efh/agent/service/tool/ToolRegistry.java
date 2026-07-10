package com.efh.agent.service.tool;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for tools that the Agent may call.
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

    /** Converts local tool metadata to OpenAI-compatible function tool schema. */
    public List<Map<String, Object>> toOpenAiTools() {
        return tools.values().stream().map(t -> {
            Map<String, Object> fn = new HashMap<>();
            fn.put("name", t.name());
            fn.put("description", t.description());
            fn.put("parameters", buildParameters(t.name()));
            Map<String, Object> tool = new HashMap<>();
            tool.put("type", "function");
            tool.put("function", fn);
            return tool;
        }).collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> buildParameters(String toolName) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        if ("order_query".equals(toolName)) {
            props.put("orderNo", mapOf("type", "string", "description", "配件订单号，例如 PO 开头的订单号"));
        } else if ("service_ticket_create".equals(toolName)) {
            props.put("title", mapOf("type", "string", "description", "工单标题"));
            props.put("description", mapOf("type", "string", "description", "故障或服务需求描述"));
            props.put("phone", mapOf("type", "string", "description", "联系电话"));
            props.put("address", mapOf("type", "string", "description", "上门服务地址"));
            props.put("serviceType", mapOf("type", "integer", "description", "1维修 2保养 3咨询"));
        } else {
            props.put("query", mapOf("type", "string", "description", "搜索关键词或用户问题"));
        }
        params.put("properties", props);
        return params;
    }

    private Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }
}
