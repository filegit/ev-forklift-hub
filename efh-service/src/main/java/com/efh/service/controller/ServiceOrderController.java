package com.efh.service.controller;

import com.efh.common.result.Result;
import com.efh.common.utils.UserContextUtil;
import com.efh.service.entity.ServiceOrder;
import com.efh.service.service.ServiceOrderService;
import com.efh.service.vo.ServiceOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service/order")
public class ServiceOrderController {

    @Autowired
    private ServiceOrderService serviceOrderService;

    @PostMapping
    public Result<?> createServiceOrder(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                        @Validated @RequestBody ServiceOrderVO serviceOrderVO) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        serviceOrderService.createServiceOrder(userId, serviceOrderVO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ServiceOrder>> listMyOrders(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(serviceOrderService.listUserOrders(userId));
    }

    @GetMapping("/{id}")
    public Result<ServiceOrder> getOrderDetail(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                               @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(serviceOrderService.getUserOrder(userId, id));
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancelOrder(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                 @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        serviceOrderService.cancelOrder(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/accept")
    public Result<?> acceptOrder(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                 @PathVariable Long id) {
        Long technicianId = UserContextUtil.requireUserId(userIdHeader);
        serviceOrderService.acceptOrder(technicianId, id);
        return Result.success();
    }

    @PostMapping("/{id}/complete")
    public Result<?> completeOrder(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                   @PathVariable Long id) {
        Long technicianId = UserContextUtil.requireUserId(userIdHeader);
        serviceOrderService.completeOrder(technicianId, id);
        return Result.success();
    }
}
