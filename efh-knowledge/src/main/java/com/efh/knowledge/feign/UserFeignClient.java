package com.efh.knowledge.feign;

import com.efh.common.result.Result;
import com.efh.knowledge.feign.dto.UserBriefDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "efh-user", url = "${efh.user.url}")
public interface UserFeignClient {

    @GetMapping("/api/internal/user/brief")
    Result<UserBriefDTO> getUserBrief(@RequestHeader("X-User-Id") String userId);

    @PostMapping("/api/internal/points/consume")
    Result<Void> consumePoints(@RequestHeader("X-User-Id") String userId, @RequestBody Map<String, Object> body);
}
