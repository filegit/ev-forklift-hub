package com.efh.parts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.parts.entity.PartsPayment;
import com.efh.parts.vo.PayPageVO;
import com.efh.parts.vo.PayStatusVO;

import java.util.Map;

public interface PaymentService extends IService<PartsPayment> {

    PartsPayment createPayment(Long userId, Long orderId);

    PayPageVO createAlipayPagePay(Long userId, String payNo);

    String handleAlipayNotify(Map<String, String> params);

    PayStatusVO getPayStatus(Long userId, String payNo);

    /** @deprecated 仅开发调试，生产请走支付宝 */
    void mockPaySuccess(Long userId, String payNo);
}
