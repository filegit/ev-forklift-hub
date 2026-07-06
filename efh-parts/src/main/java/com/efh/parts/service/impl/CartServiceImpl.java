package com.efh.parts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.parts.entity.CartItem;
import com.efh.parts.entity.Parts;
import com.efh.parts.mapper.CartItemMapper;
import com.efh.parts.service.CartService;
import com.efh.parts.service.PartsService;
import com.efh.parts.vo.CartAddVO;
import com.efh.parts.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {

    @Autowired
    private PartsService partsService;

    @Override
    public List<CartItemVO> listCart(Long userId) {
        List<CartItem> items = list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getUpdateTime));
        List<CartItemVO> result = new ArrayList<>();
        for (CartItem item : items) {
            CartItemVO vo = buildCartItemVO(item);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public int countCart(Long userId) {
        return (int) count(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    @Override
    public void addToCart(Long userId, CartAddVO vo) {
        Parts parts = partsService.getById(vo.getPartsId());
        if (parts == null || parts.getStatus() == 0) {
            throw new BusinessException("配件不存在或已下架");
        }
        if (parts.getStock() < vo.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        CartItem existing = getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getPartsId, vo.getPartsId()));
        if (existing != null) {
            int newQty = existing.getQuantity() + vo.getQuantity();
            if (newQty > parts.getStock()) {
                throw new BusinessException("库存不足");
            }
            existing.setQuantity(newQty);
            updateById(existing);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setPartsId(vo.getPartsId());
            item.setQuantity(vo.getQuantity());
            save(item);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long partsId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException("数量无效");
        }
        CartItem item = getCartItem(userId, partsId);
        Parts parts = partsService.getById(partsId);
        if (parts == null || parts.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        item.setQuantity(quantity);
        updateById(item);
    }

    @Override
    public void removeItem(Long userId, Long partsId) {
        remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getPartsId, partsId));
    }

    @Override
    public void clearItems(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }
        remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, cartItemIds));
    }

    public List<CartItemVO> listByIds(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<CartItem> items = list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, cartItemIds));
        return items.stream().map(this::buildCartItemVO).filter(v -> v != null).collect(Collectors.toList());
    }

    private CartItem getCartItem(Long userId, Long partsId) {
        CartItem item = getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getPartsId, partsId));
        if (item == null) {
            throw new BusinessException("购物车商品不存在");
        }
        return item;
    }

    private CartItemVO buildCartItemVO(CartItem item) {
        Parts parts = partsService.getById(item.getPartsId());
        if (parts == null) {
            return null;
        }
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setPartsId(parts.getId());
        vo.setName(parts.getName());
        vo.setImage(firstImage(parts.getImages()));
        vo.setPrice(parts.getPrice());
        vo.setStock(parts.getStock());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(parts.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        vo.setStatus(parts.getStatus());
        vo.setSellerId(parts.getSellerId());
        return vo;
    }

    static String firstImage(String images) {
        if (images == null || images.isEmpty()) {
            return "https://via.placeholder.com/200";
        }
        return images.split(",")[0];
    }
}
