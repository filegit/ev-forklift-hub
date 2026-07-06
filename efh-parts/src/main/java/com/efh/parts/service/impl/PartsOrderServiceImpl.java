package com.efh.parts.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.parts.entity.Parts;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.entity.PartsOrderItem;
import com.efh.parts.entity.PartsPayment;
import com.efh.parts.mapper.PartsOrderMapper;
import com.efh.parts.service.CartService;
import com.efh.parts.service.PartsOrderItemService;
import com.efh.parts.service.PartsOrderService;
import com.efh.parts.service.PartsService;
import com.efh.parts.service.PaymentService;
import com.efh.parts.service.ShipmentService;
import com.efh.parts.vo.CartItemVO;
import com.efh.parts.vo.OrderDetailVO;
import com.efh.parts.vo.OrderPreviewVO;
import com.efh.parts.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PartsOrderServiceImpl extends ServiceImpl<PartsOrderMapper, PartsOrder> implements PartsOrderService {

    private static final BigDecimal FREIGHT = new BigDecimal("0.00");

    @Value("${parts.order.pay-timeout-minutes:30}")
    private int payTimeoutMinutes;

    @Autowired
    private PartsService partsService;

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private PartsOrderItemService orderItemService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ShipmentService shipmentService;

    @Override
    public OrderPreviewVO previewOrder(Long userId, OrderSubmitVO vo) {
        List<CartItemVO> items = resolveItems(userId, vo);
        if (items.isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }
        validateStock(items);

        BigDecimal total = items.stream()
                .map(CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderPreviewVO preview = new OrderPreviewVO();
        preview.setItems(items);
        preview.setTotalAmount(total);
        preview.setFreightAmount(FREIGHT);
        preview.setPayAmount(total.add(FREIGHT));
        preview.setReceiverName(vo.getReceiverName());
        preview.setReceiverPhone(vo.getReceiverPhone());
        preview.setReceiverAddress(vo.getReceiverAddress());
        return preview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> submitOrder(Long userId, OrderSubmitVO vo) {
        List<CartItemVO> items = resolveItems(userId, vo);
        if (items.isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }
        validateStock(items);

        Map<Long, List<CartItemVO>> bySeller = items.stream()
                .collect(Collectors.groupingBy(CartItemVO::getSellerId, LinkedHashMap::new, Collectors.toList()));

        List<String> orderNos = new ArrayList<>();
        List<Long> cartIdsToClear = vo.getCartItemIds();

        for (Map.Entry<Long, List<CartItemVO>> entry : bySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<CartItemVO> sellerItems = entry.getValue();

            BigDecimal total = sellerItems.stream()
                    .map(CartItemVO::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal payAmount = total.add(FREIGHT);

            PartsOrder order = new PartsOrder();
            order.setOrderNo("PO" + IdUtil.getSnowflakeNextIdStr());
            order.setBuyerId(userId);
            order.setSellerId(sellerId);
            order.setTotalAmount(total);
            order.setFreightAmount(FREIGHT);
            order.setPayAmount(payAmount);
            order.setStatus(0);
            order.setReceiverName(vo.getReceiverName());
            order.setReceiverPhone(vo.getReceiverPhone());
            order.setReceiverAddress(vo.getReceiverAddress());
            order.setRemark(vo.getRemark());
            save(order);

            for (CartItemVO item : sellerItems) {
                PartsOrderItem line = new PartsOrderItem();
                line.setOrderId(order.getId());
                line.setPartsId(item.getPartsId());
                line.setPartsName(item.getName());
                line.setPartsImage(item.getImage());
                line.setPrice(item.getPrice());
                line.setQuantity(item.getQuantity());
                line.setSubtotal(item.getSubtotal());
                orderItemService.save(line);
            }

            paymentService.createPayment(userId, order.getId());
            orderNos.add(order.getOrderNo());
        }

        if (cartIdsToClear != null && !cartIdsToClear.isEmpty()) {
            cartService.clearItems(userId, cartIdsToClear);
        }

        return orderNos;
    }

    @Override
    public IPage<PartsOrder> listOrders(Long userId, Integer status, Page<PartsOrder> page) {
        LambdaQueryWrapper<PartsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PartsOrder::getBuyerId, userId);
        if (status != null && status >= 0) {
            wrapper.eq(PartsOrder::getStatus, status);
        }
        wrapper.orderByDesc(PartsOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long orderId) {
        PartsOrder order = getAndCheck(orderId, userId);
        return buildDetail(order);
    }

    @Override
    public OrderDetailVO getOrderDetailByNo(Long userId, String orderNo) {
        PartsOrder order = getOne(new LambdaQueryWrapper<PartsOrder>().eq(PartsOrder::getOrderNo, orderNo));
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return buildDetail(order);
    }

    private OrderDetailVO buildDetail(PartsOrder order) {
        Long orderId = order.getId();
        OrderDetailVO detail = new OrderDetailVO();
        detail.setOrder(order);
        detail.setItems(orderItemService.list(new LambdaQueryWrapper<PartsOrderItem>()
                .eq(PartsOrderItem::getOrderId, orderId)));
        detail.setPayment(paymentService.getOne(new LambdaQueryWrapper<PartsPayment>()
                .eq(PartsPayment::getOrderId, orderId)
                .orderByDesc(PartsPayment::getCreateTime)
                .last("LIMIT 1")));
        detail.setShipment(shipmentService.getByOrderId(orderId));
        if (detail.getShipment() != null) {
            detail.setTraces(shipmentService.listTraces(detail.getShipment().getId()));
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        PartsOrder order = getAndCheck(orderId, userId);
        if (order.getStatus() != 0) {
            throw new BusinessException("当前状态不可取消");
        }
        order.setStatus(4);
        order.setCancelTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long userId, Long orderId) {
        PartsOrder order = getAndCheck(orderId, userId);
        if (order.getStatus() != 2) {
            throw new BusinessException("当前状态不可确认收货");
        }
        order.setStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        updateById(order);
        shipmentService.markReceived(orderId);
    }

    @Override
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(payTimeoutMinutes);
        List<PartsOrder> timeoutOrders = list(new LambdaQueryWrapper<PartsOrder>()
                .eq(PartsOrder::getStatus, 0)
                .lt(PartsOrder::getCreateTime, deadline));
        for (PartsOrder order : timeoutOrders) {
            order.setStatus(4);
            order.setCancelTime(LocalDateTime.now());
            updateById(order);
        }
    }

    private PartsOrder getAndCheck(Long orderId, Long userId) {
        PartsOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        return order;
    }

    private List<CartItemVO> resolveItems(Long userId, OrderSubmitVO vo) {
        if (vo.getCartItemIds() != null && !vo.getCartItemIds().isEmpty()) {
            return cartService.listByIds(userId, vo.getCartItemIds());
        }
        if (vo.getDirectItems() != null && !vo.getDirectItems().isEmpty()) {
            List<CartItemVO> list = new ArrayList<>();
            for (OrderSubmitVO.DirectItem di : vo.getDirectItems()) {
                Parts parts = partsService.getById(di.getPartsId());
                if (parts == null || parts.getStatus() == 0) {
                    throw new BusinessException("配件不存在或已下架");
                }
                CartItemVO item = new CartItemVO();
                item.setPartsId(parts.getId());
                item.setName(parts.getName());
                item.setImage(CartServiceImpl.firstImage(parts.getImages()));
                item.setPrice(parts.getPrice());
                item.setStock(parts.getStock());
                item.setQuantity(di.getQuantity());
                item.setSubtotal(parts.getPrice().multiply(BigDecimal.valueOf(di.getQuantity())));
                item.setStatus(parts.getStatus());
                item.setSellerId(parts.getSellerId());
                list.add(item);
            }
            return list;
        }
        return new ArrayList<>();
    }

    private void validateStock(List<CartItemVO> items) {
        for (CartItemVO item : items) {
            if (item.getStatus() == null || item.getStatus() == 0) {
                throw new BusinessException("商品已下架: " + item.getName());
            }
            if (item.getStock() < item.getQuantity()) {
                throw new BusinessException("库存不足: " + item.getName());
            }
        }
    }
}
