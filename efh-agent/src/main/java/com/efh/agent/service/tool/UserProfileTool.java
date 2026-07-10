package com.efh.agent.service.tool;

import com.efh.agent.feign.UserFeignClient;
import com.efh.agent.feign.dto.UserBriefDTO;
import com.efh.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserProfileTool implements AgentTool {

    @Autowired
    private UserFeignClient userFeignClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String name() {
        return "user_profile_query";
    }

    @Override
    public String description() {
        return "查询当前登录用户的基础信息、用户类型和昵称，无需参数";
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
        try {
            Result<UserBriefDTO> result = userFeignClient.getUserBrief(String.valueOf(ctx.getUserId()));
            if (result == null || result.getCode() != 200) {
                return "用户信息查询失败：" + (result == null ? "用户服务无响应" : result.getMessage());
            }
            return objectMapper.writeValueAsString(result.getData());
        } catch (Exception e) {
            return "用户信息工具异常：" + e.getMessage();
        }
    }
}
