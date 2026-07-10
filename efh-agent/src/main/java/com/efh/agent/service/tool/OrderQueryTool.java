package com.efh.agent.service.tool;

import com.efh.agent.feign.PartsFeignClient;
import com.efh.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderQueryTool implements AgentTool {

    @Autowired
    private PartsFeignClient partsFeignClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String name() {
        return "order_query";
    }

    @Override
    public String description() {
        return "查询配件订单、支付状态和物流状态，参数 orderNo";
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public String execute(ToolContext ctx, Map<String, Object> args) {
        String orderNo = String.valueOf(args.getOrDefault("orderNo", args.getOrDefault("query", ""))).trim();
        if (orderNo.isEmpty()) {
            return "订单查询失败：缺少订单号，请让用户提供 PO/PAY/ORD 开头的订单号。";
        }
        try {
            Result<Map<String, Object>> result = partsFeignClient.getOrderByNo(String.valueOf(ctx.getUserId()), orderNo);
            if (result == null || result.getCode() != 200) {
                return "订单查询失败：" + (result == null ? "订单服务无响应" : result.getMessage());
            }
            return objectMapper.writeValueAsString(result.getData());
        } catch (Exception e) {
            return "订单查询工具异常：" + e.getMessage() + "。请引导用户到订单中心查询或稍后重试。";
        }
    }
}
