package com.efh.community.feign;

import com.efh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "efh-user", url = "${efh.user.url:http://127.0.0.1:8081}")
public interface UserFeignClient {

    @PostMapping("/api/internal/points/add")
    Result<Void> addPoints(@RequestHeader("X-User-Id") String userId, @RequestBody Map<String, Object> body);
}
