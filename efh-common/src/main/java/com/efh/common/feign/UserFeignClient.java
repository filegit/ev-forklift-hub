package com.efh.common.feign;

import com.efh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "efh-user", path = "/user")
public interface UserFeignClient {

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    Result getUserById(@PathVariable("id") Long id);
}
