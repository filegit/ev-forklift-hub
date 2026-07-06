package com.efh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.user.entity.PointsExchange;
import com.efh.user.mapper.PointsExchangeMapper;
import com.efh.user.service.PointsExchangeService;
import com.efh.user.service.UserPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 积分兑换服务实现
 */
@Slf4j
@Service
public class PointsExchangeServiceImpl extends ServiceImpl<PointsExchangeMapper, PointsExchange> implements PointsExchangeService {

    private static final Map<Long, ExchangeItem> CATALOG;

    static {
        Map<Long, ExchangeItem> catalog = new HashMap<>();
        catalog.put(1L, new ExchangeItem("优惠券", "100元优惠券", 500));
        catalog.put(2L, new ExchangeItem("会员卡", "一个月会员", 1000));
        catalog.put(3L, new ExchangeItem("礼品卡", "200元礼品卡", 2000));
        CATALOG = Collections.unmodifiableMap(catalog);
    }
    
    @Autowired
    private UserPointsService userPointsService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchange(Long userId, Long exchangeId) {
        log.info("兑换积分: userId={}, exchangeId={}", userId, exchangeId);
        
        ExchangeItem item = CATALOG.get(exchangeId);
        if (item == null) {
            throw new BusinessException("兑换商品不存在");
        }
        
        if (!userPointsService.hasEnoughPoints(userId, item.points)) {
            throw new BusinessException("积分不足");
        }
        
        userPointsService.consumePoints(userId, item.points, "兑换: " + item.name);
        
        PointsExchange record = new PointsExchange();
        record.setUserId(userId);
        record.setItemName(item.name);
        record.setPoints(item.points);
        record.setStatus("pending");
        
        this.save(record);
        log.info("兑换成功: userId={}, itemName={}", userId, item.name);
    }
    
    @Override
    public IPage<PointsExchange> getUserExchanges(Long userId, Page<PointsExchange> page) {
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsExchange::getUserId, userId)
               .orderByDesc(PointsExchange::getCreateTime);
        
        return this.page(page, wrapper);
    }

    private static class ExchangeItem {
        private final String name;
        private final String description;
        private final int points;

        private ExchangeItem(String name, String description, int points) {
            this.name = name;
            this.description = description;
            this.points = points;
        }
    }
}
