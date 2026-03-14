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

/**
 * 积分兑换服务实现
 */
@Slf4j
@Service
public class PointsExchangeServiceImpl extends ServiceImpl<PointsExchangeMapper, PointsExchange> implements PointsExchangeService {
    
    @Autowired
    private UserPointsService userPointsService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchange(Long userId, Long exchangeId) {
        log.info("兑换积分: userId={}, exchangeId={}", userId, exchangeId);
        
        PointsExchange exchange = this.getById(exchangeId);
        if (exchange == null) {
            throw new BusinessException("兑换商品不存在");
        }
        
        // 检查积分是否足够
        if (!userPointsService.hasEnoughPoints(userId, exchange.getPoints())) {
            throw new BusinessException("积分不足");
        }
        
        // 消耗积分
        userPointsService.consumePoints(userId, exchange.getPoints(), "兑换: " + exchange.getItemName());
        
        // 创建兑换记录
        PointsExchange record = new PointsExchange();
        record.setUserId(userId);
        record.setItemName(exchange.getItemName());
        record.setPoints(exchange.getPoints());
        record.setStatus("pending");
        
        this.save(record);
        log.info("兑换成功: userId={}, itemName={}", userId, exchange.getItemName());
    }
    
    @Override
    public IPage<PointsExchange> getUserExchanges(Long userId, Page<PointsExchange> page) {
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsExchange::getUserId, userId)
               .orderByDesc(PointsExchange::getCreateTime);
        
        return this.page(page, wrapper);
    }
}
