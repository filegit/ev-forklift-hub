package com.efh.parts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.parts.entity.Parts;
import com.efh.parts.vo.PartsVO;

public interface PartsService extends IService<Parts> {
    
    /**
     * 发布零部件
     */
    void publishParts(Long sellerId, PartsVO partsVO);
    
    /**
     * 零部件列表
     */
    IPage<Parts> getPartsList(Page<Parts> page, String category, String keyword);
    
    /**
     * 零部件详情
     */
    Parts getPartsDetail(Long id);
}
