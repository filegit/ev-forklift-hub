package com.efh.parts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.parts.entity.PartsOrder;
import com.efh.parts.vo.OrderDetailVO;
import com.efh.parts.vo.OrderPreviewVO;
import com.efh.parts.vo.OrderSubmitVO;

import java.util.List;

public interface PartsOrderService extends IService<PartsOrder> {

    OrderPreviewVO previewOrder(Long userId, OrderSubmitVO vo);

    List<String> submitOrder(Long userId, OrderSubmitVO vo);

    IPage<PartsOrder> listOrders(Long userId, Integer status, Page<PartsOrder> page);

    OrderDetailVO getOrderDetail(Long userId, Long orderId);

    OrderDetailVO getOrderDetailByNo(Long userId, String orderNo);

    void cancelOrder(Long userId, Long orderId);

    void confirmReceive(Long userId, Long orderId);

    void cancelTimeoutOrders();
}
