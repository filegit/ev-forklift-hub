package com.efh.knowledge.service;

import com.efh.common.exception.BusinessException;
import com.efh.common.result.Result;
import com.efh.knowledge.config.KnowledgeProperties;
import com.efh.knowledge.constant.KnowledgeConstants;
import com.efh.knowledge.feign.UserFeignClient;
import com.efh.knowledge.feign.dto.UserBriefDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private KnowledgeProperties knowledgeProperties;

    public UserBriefDTO requireAdmin(Long userId) {
        UserBriefDTO user = getUserBrief(userId);
        if (user.getUserType() == null || user.getUserType() != knowledgeProperties.getAdminUserType()) {
            throw new BusinessException(403, "需要管理员权限");
        }
        return user;
    }

    public UserBriefDTO getUserBrief(Long userId) {
        Result<UserBriefDTO> result = userFeignClient.getUserBrief(String.valueOf(userId));
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException("用户信息获取失败");
        }
        return result.getData();
    }
}
