package com.efh.service.controller;

import com.efh.common.result.Result;
import com.efh.common.utils.JwtUtil;
import com.efh.service.service.ServiceOrderService;
import com.efh.service.vo.ServiceOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service/order")
public class ServiceOrderController {
    
    @Autowired
    private ServiceOrderService serviceOrderService;
    
    /**
     * 创建服务工单
     */
    @PostMapping
    public Result<?> createServiceOrder(@RequestHeader("Authorization") String token,
                                        @RequestBody ServiceOrderVO serviceOrderVO) {
        Long userId = JwtUtil.getUserId(token);
        serviceOrderService.createServiceOrder(userId, serviceOrderVO);
        return Result.success();
    }
    
    /**
     * 技师接单
     */
    @PostMapping("/{id}/accept")
    public Result<?> acceptOrder(@RequestHeader("Authorization") String token,
                                 @PathVariable Long id) {
        Long technicianId = JwtUtil.getUserId(token);
        serviceOrderService.acceptOrder(technicianId, id);
        return Result.success();
    }
    
    /**
     * 完成工单
     */
    @PostMapping("/{id}/complete")
    public Result<?> completeOrder(@RequestHeader("Authorization") String token,
                                   @PathVariable Long id) {
        Long technicianId = JwtUtil.getUserId(token);
        serviceOrderService.completeOrder(technicianId, id);
        return Result.success();
    }
}
