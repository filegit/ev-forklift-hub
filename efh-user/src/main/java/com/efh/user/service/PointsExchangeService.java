package com.efh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.user.entity.PointsExchange;

/**
 * 积分兑换服务接口
 */
public interface PointsExchangeService extends IService<PointsExchange> {
    
    /**
     * 兑换积分
     */
    void exchange(Long userId, Long exchangeId);
    
    /**
     * 获取用户兑换记录
     */
    IPage<PointsExchange> getUserExchanges(Long userId, Page<PointsExchange> page);
}
