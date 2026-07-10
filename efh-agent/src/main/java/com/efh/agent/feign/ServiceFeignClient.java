package com.efh.agent.feign;

import com.efh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "efh-service", url = "${efh.service.url:http://127.0.0.1:8084}")
public interface ServiceFeignClient {

    @PostMapping("/api/service/order")
    Result<Object> createOrder(@RequestHeader("X-User-Id") String userId,
                               @RequestBody Map<String, Object> body);
}
