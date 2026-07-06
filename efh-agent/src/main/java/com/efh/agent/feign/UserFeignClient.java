package com.efh.agent.feign;

import com.efh.agent.feign.dto.UserBriefDTO;
import com.efh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "efh-user", url = "${efh.user.url:http://127.0.0.1:8081}")
public interface UserFeignClient {

    @GetMapping("/api/internal/user/brief")
    Result<UserBriefDTO> getUserBrief(@RequestHeader("X-User-Id") String userId);
}
