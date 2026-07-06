package com.efh.parts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.parts.entity.CartItem;
import com.efh.parts.vo.CartAddVO;
import com.efh.parts.vo.CartItemVO;

import java.util.List;

public interface CartService extends IService<CartItem> {

    List<CartItemVO> listCart(Long userId);

    int countCart(Long userId);

    void addToCart(Long userId, CartAddVO vo);

    void updateQuantity(Long userId, Long partsId, Integer quantity);

    void removeItem(Long userId, Long partsId);

    void clearItems(Long userId, List<Long> cartItemIds);
}
