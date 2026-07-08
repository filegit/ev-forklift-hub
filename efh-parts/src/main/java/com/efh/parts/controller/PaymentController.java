package com.efh.parts.controller;

import com.efh.common.result.Result;
import com.efh.common.utils.UserContextUtil;
import com.efh.parts.entity.PartsPayment;
import com.efh.parts.service.PaymentService;
import com.efh.parts.vo.PayPageVO;
import com.efh.parts.vo.PayStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/parts/pay")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public Result<PartsPayment> create(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @RequestParam Long orderId) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(paymentService.createPayment(userId, orderId));
    }

    /** 发起支付宝电脑网站支付，返回自动提交的 HTML 表单 */
    @PostMapping("/alipay/page")
    public Result<PayPageVO> alipayPage(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                        @RequestParam String payNo) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(paymentService.createAlipayPagePay(userId, payNo));
    }

    /** 支付宝异步通知（公网回调，无需登录） */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        return paymentService.handleAlipayNotify(extractParams(request));
    }

    /** 查询支付状态（支付结果页轮询） */
    @GetMapping("/status")
    public Result<PayStatusVO> payStatus(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                         @RequestParam String payNo) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(paymentService.getPayStatus(userId, payNo));
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }
}
