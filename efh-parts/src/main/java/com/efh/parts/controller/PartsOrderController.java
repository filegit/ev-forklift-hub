package com.efh.parts.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.common.utils.UserContextUtil;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.service.PartsOrderService;
import com.efh.parts.vo.OrderDetailVO;
import com.efh.parts.vo.OrderPreviewVO;
import com.efh.parts.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parts/order")
public class PartsOrderController {

    @Autowired
    private PartsOrderService partsOrderService;

    @PostMapping("/preview")
    public Result<OrderPreviewVO> preview(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                          @RequestBody OrderSubmitVO vo) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(partsOrderService.previewOrder(userId, vo));
    }

    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                              @Validated @RequestBody OrderSubmitVO vo) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        List<String> orderNos = partsOrderService.submitOrder(userId, vo);
        Map<String, Object> data = new HashMap<>();
        data.put("orderNos", orderNos);
        data.put("orderNo", orderNos.isEmpty() ? null : orderNos.get(0));
        return Result.success(data);
    }

    @GetMapping("/list")
    public Result<IPage<PartsOrder>> list(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Integer status) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        IPage<PartsOrder> orders = partsOrderService.listOrders(userId, status, new Page<>(page, size));
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                        @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(partsOrderService.getOrderDetail(userId, id));
    }

    @GetMapping("/no/{orderNo}")
    public Result<OrderDetailVO> detailByNo(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                            @PathVariable String orderNo) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(partsOrderService.getOrderDetailByNo(userId, orderNo));
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                            @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        partsOrderService.cancelOrder(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<?> confirm(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                             @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        partsOrderService.confirmReceive(userId, id);
        return Result.success();
    }
}
