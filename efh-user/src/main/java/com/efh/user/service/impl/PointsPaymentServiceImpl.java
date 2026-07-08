package com.efh.user.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.user.entity.PointsPayment;
import com.efh.user.mapper.PointsPaymentMapper;
import com.efh.user.service.AlipayPayService;
import com.efh.user.service.PointsPaymentService;
import com.efh.user.service.UserPointsService;
import com.efh.user.vo.PayPageVO;
import com.efh.user.vo.PointsPayStatusVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PointsPaymentServiceImpl extends ServiceImpl<PointsPaymentMapper, PointsPayment> implements PointsPaymentService {

    private static final Set<String> SUCCESS_STATUS = new HashSet<>(Arrays.asList("TRADE_SUCCESS", "TRADE_FINISHED"));

    @Autowired
    private AlipayPayService alipayPayService;

    @Autowired
    private UserPointsService userPointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayPageVO createAlipayPagePay(Long userId, Integer packageId) {
        PointsPackage pkg = requirePackage(packageId);
        PointsPayment payment = new PointsPayment();
        payment.setUserId(userId);
        payment.setPayNo("PTPAY" + IdUtil.getSnowflakeNextIdStr());
        payment.setPackageId(packageId);
        payment.setPoints(pkg.getPoints());
        payment.setAmount(pkg.getAmount());
        payment.setPayChannel("alipay");
        payment.setStatus(0);
        save(payment);

        String payForm = alipayPayService.createPagePayForm(
                payment.getPayNo(),
                payment.getAmount(),
                "积分套餐-" + pkg.getPoints() + "积分"
        );

        PayPageVO vo = new PayPageVO();
        vo.setPayNo(payment.getPayNo());
        vo.setPayForm(payForm);
        vo.setChannel("alipay");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        if (!alipayPayService.verifyNotify(params)) {
            log.warn("积分支付支付宝回调验签失败: {}", params);
            return "failure";
        }
        if (!SUCCESS_STATUS.contains(alipayPayService.getTradeStatus(params))) {
            return "success";
        }
        String payNo = alipayPayService.getOutTradeNo(params);
        String tradeNo = alipayPayService.getTradeNo(params);
        PointsPayment payment = getOne(new LambdaQueryWrapper<PointsPayment>().eq(PointsPayment::getPayNo, payNo));
        if (payment == null) {
            return "failure";
        }
        if (payment.getStatus() != null && payment.getStatus() == 1) {
            return "success";
        }

        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        payment.setThirdTradeNo(tradeNo);
        updateById(payment);
        userPointsService.addPoints(payment.getUserId(), payment.getPoints(), "购买积分套餐(" + payment.getAmount() + "元)");
        log.info("积分支付完成 payNo={} userId={} points={}", payNo, payment.getUserId(), payment.getPoints());
        return "success";
    }

    @Override
    public PointsPayStatusVO getPayStatus(Long userId, String payNo) {
        PointsPayment payment = getOne(new LambdaQueryWrapper<PointsPayment>().eq(PointsPayment::getPayNo, payNo));
        if (payment == null || !payment.getUserId().equals(userId)) {
            throw new BusinessException("支付单不存在");
        }
        PointsPayStatusVO vo = new PointsPayStatusVO();
        vo.setPayNo(payment.getPayNo());
        vo.setPackageId(payment.getPackageId());
        vo.setPoints(payment.getPoints());
        vo.setStatus(payment.getStatus());
        vo.setPayChannel(payment.getPayChannel());
        vo.setThirdTradeNo(payment.getThirdTradeNo());
        return vo;
    }

    private PointsPackage requirePackage(Integer packageId) {
        if (packageId == null) {
            throw new BusinessException("请选择购买套餐");
        }
        switch (packageId) {
            case 1:
                return new PointsPackage(100, new BigDecimal("10.00"));
            case 2:
                return new PointsPackage(500, new BigDecimal("45.00"));
            case 3:
                return new PointsPackage(1000, new BigDecimal("88.00"));
            default:
                throw new BusinessException("无效的购买套餐");
        }
    }

    @Data
    @AllArgsConstructor
    private static class PointsPackage {
        private int points;
        private BigDecimal amount;
    }
}
