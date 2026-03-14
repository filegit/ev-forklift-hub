package com.efh.gateway.filter;

import com.efh.common.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 全局认证过滤器
 * 对所有请求进行 JWT Token 验证，白名单内的请求除外
 * 
 * @author EFH Team
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    /**
     * 白名单：无需认证的接口路径
     * - /user/api/login: 用户登录接口
     * - /user/api/register: 用户注册接口
     * - /community/api/post/list: 帖子列表（公开）
     * - /community/api/comment/list: 评论列表（公开）
     * - /actuator: 健康检查接口（网关自身）
     * - /user/actuator: 用户服务健康检查
     * - /community/actuator: 社区服务健康检查
     * - /parts/actuator: 配件服务健康检查
     * - /service/actuator: 维修服务健康检查
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/user/api/login",
            "/user/api/register",
            "/community/api/post/list",
            "/community/api/comment/list",
            "/actuator",
            "/user/actuator",
            "/community/actuator",
            "/parts/actuator",
            "/service/actuator"
    );

    /**
     * 过滤器核心逻辑
     * 
     * @param exchange 当前请求的上下文
     * @param chain 过滤器链
     * @return Mono<Void> 异步响应
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单路径直接放行
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 2. 获取请求头中的 Authorization Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            // Token 不存在，返回 401 未授权
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 3. 验证 Token 并提取用户信息
        try {
            // 去除 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 解析 Token 获取用户ID
            Long userId = JwtUtil.getUserId(token);
            
            // 验证 Token 是否过期
            if (JwtUtil.isTokenExpired(token)) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            
            // 4. 将用户ID传递到下游服务（通过请求头）
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .build();
            
            // 5. 继续执行过滤器链
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (Exception e) {
            // Token 解析失败，返回 401 未授权
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    /**
     * 过滤器执行顺序
     * 数值越小，优先级越高
     * 
     * @return -100（高优先级，在其他过滤器之前执行）
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
