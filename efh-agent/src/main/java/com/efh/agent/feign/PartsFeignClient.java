package com.efh.agent.feign;

import com.efh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "efh-parts", url = "${efh.parts.url:http://127.0.0.1:8083}")
public interface PartsFeignClient {

    @GetMapping("/api/parts/order/no/{orderNo}")
    Result<Map<String, Object>> getOrderByNo(@RequestHeader("X-User-Id") String userId,
                                             @PathVariable("orderNo") String orderNo);
}
