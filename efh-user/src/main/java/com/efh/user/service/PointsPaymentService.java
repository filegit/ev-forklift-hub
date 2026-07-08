package com.efh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.user.entity.PointsPayment;
import com.efh.user.vo.PayPageVO;
import com.efh.user.vo.PointsPayStatusVO;

import java.util.Map;

public interface PointsPaymentService extends IService<PointsPayment> {
    PayPageVO createAlipayPagePay(Long userId, Integer packageId);
    String handleAlipayNotify(Map<String, String> params);
    PointsPayStatusVO getPayStatus(Long userId, String payNo);
}
