package com.efh.parts.controller;

import com.efh.common.result.Result;
import com.efh.common.utils.JwtUtil;
import com.efh.parts.service.PartsOrderService;
import com.efh.parts.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parts/order")
public class PartsOrderController {
    
    @Autowired
    private PartsOrderService partsOrderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public Result<?> createOrder(@RequestHeader("Authorization") String token,
                                 @RequestBody OrderVO orderVO) {
        Long userId = JwtUtil.getUserId(token);
        partsOrderService.createOrder(userId, orderVO);
        return Result.success();
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<?> cancelOrder(@RequestHeader("Authorization") String token,
                                 @PathVariable Long id) {
        Long userId = JwtUtil.getUserId(token);
        partsOrderService.cancelOrder(userId, id);
        return Result.success();
    }
}
