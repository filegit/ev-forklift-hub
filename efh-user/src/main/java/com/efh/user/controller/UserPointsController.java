package com.efh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.user.entity.PointsExchange;
import com.efh.user.entity.UserPoints;
import com.efh.user.service.PointsExchangeService;
import com.efh.user.service.PointsPaymentService;
import com.efh.user.service.UserPointsService;
import com.efh.user.vo.PayPageVO;
import com.efh.user.vo.PointsPayStatusVO;
import com.efh.user.vo.PointsPurchaseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/points")
public class UserPointsController {

    @Autowired
    private UserPointsService userPointsService;

    @Autowired
    private PointsExchangeService pointsExchangeService;

    @Autowired
    private PointsPaymentService pointsPaymentService;

    @GetMapping
    public Result<UserPoints> getUserPoints(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseRequiredUserId(userIdHeader);
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        return Result.success(userPointsService.getUserPoints(userId));
    }

    @PostMapping("/exchange/{exchangeId}")
    public Result<?> exchange(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                              @PathVariable Long exchangeId) {
        Long userId = parseRequiredUserId(userIdHeader);
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        pointsExchangeService.exchange(userId, exchangeId);
        return Result.success(null);
    }

    @GetMapping("/exchanges")
    public Result<IPage<PointsExchange>> getExchanges(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        Long userId = parseRequiredUserId(userIdHeader);
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        return Result.success(pointsExchangeService.getUserExchanges(userId, new Page<>(page, size)));
    }

    @PostMapping("/purchase")
    public Result<PayPageVO> purchase(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                      @Validated @RequestBody PointsPurchaseVO vo) {
        Long userId = parseRequiredUserId(userIdHeader);
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        return Result.success(pointsPaymentService.createAlipayPagePay(userId, vo.getPackageId()));
    }

    @GetMapping("/pay/status")
    public Result<PointsPayStatusVO> payStatus(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                               @RequestParam String payNo) {
        Long userId = parseRequiredUserId(userIdHeader);
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        return Result.success(pointsPaymentService.getPayStatus(userId, payNo));
    }

    @PostMapping("/pay/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return pointsPaymentService.handleAlipayNotify(params);
    }

    private Long parseRequiredUserId(String userIdHeader) {
        return userIdHeader == null || userIdHeader.trim().isEmpty() ? null : Long.parseLong(userIdHeader);
    }
}
