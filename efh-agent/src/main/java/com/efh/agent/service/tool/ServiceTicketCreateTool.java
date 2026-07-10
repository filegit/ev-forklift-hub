package com.efh.agent.service.tool;

import com.efh.agent.feign.ServiceFeignClient;
import com.efh.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceTicketCreateTool implements AgentTool {

    @Autowired
    private ServiceFeignClient serviceFeignClient;

    @Override
    public String name() {
        return "service_ticket_create";
    }

    @Override
    public String description() {
        return "创建维修/保养/咨询工单，参数 title、description、phone、address、serviceType";
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
        String description = String.valueOf(args.getOrDefault("description", args.getOrDefault("query", ""))).trim();
        String phone = String.valueOf(args.getOrDefault("phone", "")).trim();
        String address = String.valueOf(args.getOrDefault("address", "")).trim();
        if (description.isEmpty()) {
            return "还需要补充具体故障现象或服务需求，我才能帮你整理售后工单。";
        }
        if (phone.isEmpty() || address.isEmpty()) {
            return "工单暂未创建：还需要用户补充联系电话和上门地址。";
        }
        Map<String, Object> body = new HashMap<>();
        body.put("serviceType", parseInt(args.get("serviceType"), 1));
        body.put("title", String.valueOf(args.getOrDefault("title", "AI客服转维修工单")));
        body.put("description", description);
        body.put("phone", phone);
        body.put("address", address);
        body.put("images", String.valueOf(args.getOrDefault("images", "")));
        try {
            Result<Object> result = serviceFeignClient.createOrder(String.valueOf(ctx.getUserId()), body);
            if (result == null || result.getCode() != 200) {
                return "工单暂时没有创建成功，请稍后重试，或者联系人工客服处理。";
            }
            return "工单已创建成功，售后人员会根据联系方式和地址跟进。";
        } catch (Exception e) {
            return "工单服务暂时不可用，请稍后重试，或者联系人工客服处理。";
        }
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
