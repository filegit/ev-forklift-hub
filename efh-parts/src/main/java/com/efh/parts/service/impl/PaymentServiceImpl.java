package com.efh.parts.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.parts.entity.Parts;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.entity.PartsOrderItem;
import com.efh.parts.entity.PartsPayment;
import com.efh.parts.mapper.PartsPaymentMapper;
import com.efh.parts.service.AlipayPayService;
import com.efh.parts.service.PartsOrderItemService;
import com.efh.parts.service.PartsOrderService;
import com.efh.parts.service.PartsService;
import com.efh.parts.service.PaymentService;
import com.efh.parts.vo.PayPageVO;
import com.efh.parts.vo.PayStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PaymentServiceImpl extends ServiceImpl<PartsPaymentMapper, PartsPayment> implements PaymentService {

    private static final Set<String> SUCCESS_STATUS = new HashSet<>(Arrays.asList("TRADE_SUCCESS", "TRADE_FINISHED"));

    @Autowired
    @Lazy
    private PartsOrderService partsOrderService;

    @Autowired
    private PartsOrderItemService orderItemService;

    @Autowired
    private PartsService partsService;

    @Autowired
    private AlipayPayService alipayPayService;

    @Override
    public PartsPayment createPayment(Long userId, Long orderId) {
        PartsOrder order = partsOrderService.getById(orderId);
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        PartsPayment existing = getOne(new LambdaQueryWrapper<PartsPayment>()
                .eq(PartsPayment::getOrderId, orderId)
                .eq(PartsPayment::getStatus, 0)
                .orderByDesc(PartsPayment::getCreateTime)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        PartsPayment payment = new PartsPayment();
        payment.setOrderId(orderId);
        payment.setOrderNo(order.getOrderNo());
        payment.setPayNo("PAY" + IdUtil.getSnowflakeNextIdStr());
        payment.setPayChannel("pending");
        payment.setAmount(order.getPayAmount());
        payment.setStatus(0);
        save(payment);
        return payment;
    }

    @Override
    public PayPageVO createAlipayPagePay(Long userId, String payNo) {
        PartsPayment payment = requirePendingPayment(userId, payNo);
        payment.setPayChannel("alipay");
        updateById(payment);

        String payForm = alipayPayService.createPagePayForm(
                payment.getPayNo(),
                payment.getAmount(),
                payment.getOrderNo()
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
            log.warn("支付宝回调验签失败: {}", params);
            return "failure";
        }
        String tradeStatus = alipayPayService.getTradeStatus(params);
        if (!SUCCESS_STATUS.contains(tradeStatus)) {
            return "success";
        }
        String payNo = alipayPayService.getOutTradeNo(params);
        String tradeNo = alipayPayService.getTradeNo(params);
        try {
            completePaymentByPayNo(payNo, tradeNo, null);
            return "success";
        } catch (BusinessException e) {
            log.error("支付宝回调处理失败 payNo={}: {}", payNo, e.getMessage());
            return "failure";
        }
    }

    @Override
    public PayStatusVO getPayStatus(Long userId, String payNo) {
        PartsPayment payment = getOne(new LambdaQueryWrapper<PartsPayment>().eq(PartsPayment::getPayNo, payNo));
        if (payment == null) {
            throw new BusinessException("支付单不存在");
        }
        PartsOrder order = partsOrderService.getById(payment.getOrderId());
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权查看");
        }
        PayStatusVO vo = new PayStatusVO();
        vo.setPayNo(payment.getPayNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setOrderId(payment.getOrderId());
        vo.setStatus(payment.getStatus());
        vo.setPayChannel(payment.getPayChannel());
        vo.setThirdTradeNo(payment.getThirdTradeNo());
        return vo;
    }

    private PartsPayment requirePendingPayment(Long userId, String payNo) {
        PartsPayment payment = getOne(new LambdaQueryWrapper<PartsPayment>().eq(PartsPayment::getPayNo, payNo));
        if (payment == null) {
            throw new BusinessException("支付单不存在");
        }
        if (payment.getStatus() == 1) {
            throw new BusinessException("订单已支付");
        }
        PartsOrder order = partsOrderService.getById(payment.getOrderId());
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态异常");
        }
        return payment;
    }

    private void completePaymentByPayNo(String payNo, String thirdTradeNo, Long userId) {
        PartsPayment payment = getOne(new LambdaQueryWrapper<PartsPayment>().eq(PartsPayment::getPayNo, payNo));
        if (payment == null) {
            throw new BusinessException("支付单不存在");
        }
        if (payment.getStatus() == 1) {
            return;
        }
        PartsOrder order = partsOrderService.getById(payment.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (userId != null && !order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态异常");
        }

        deductStock(order.getId());

        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        payment.setThirdTradeNo(thirdTradeNo);
        if (payment.getPayChannel() == null || "pending".equals(payment.getPayChannel())) {
            payment.setPayChannel("alipay");
        }
        updateById(payment);

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        partsOrderService.updateById(order);

        log.info("支付完成 payNo={} tradeNo={} orderNo={}", payNo, thirdTradeNo, order.getOrderNo());
    }

    private void deductStock(Long orderId) {
        List<PartsOrderItem> items = orderItemService.list(new LambdaQueryWrapper<PartsOrderItem>()
                .eq(PartsOrderItem::getOrderId, orderId));
        for (PartsOrderItem item : items) {
            Parts parts = partsService.getById(item.getPartsId());
            if (parts == null || parts.getStock() < item.getQuantity()) {
                throw new BusinessException("库存不足: " + item.getPartsName());
            }
            parts.setStock(parts.getStock() - item.getQuantity());
            parts.setSalesCount((parts.getSalesCount() == null ? 0 : parts.getSalesCount()) + item.getQuantity());
            partsService.updateById(parts);
        }
    }
}
