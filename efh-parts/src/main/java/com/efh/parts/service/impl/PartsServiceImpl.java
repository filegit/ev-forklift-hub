package com.efh.parts.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.parts.entity.Parts;
import com.efh.parts.mapper.PartsMapper;
import com.efh.parts.service.PartsService;
import com.efh.parts.vo.PartsVO;
import org.springframework.stereotype.Service;

@Service
public class PartsServiceImpl extends ServiceImpl<PartsMapper, Parts> implements PartsService {
    
    @Override
    public void publishParts(Long sellerId, PartsVO partsVO) {
        Parts parts = new Parts();
        parts.setSellerId(sellerId);
        parts.setName(partsVO.getName());
        parts.setDescription(partsVO.getDescription());
        parts.setCategory(partsVO.getCategory());
        parts.setBrand(partsVO.getBrand());
        parts.setModel(partsVO.getModel());
        parts.setPrice(partsVO.getPrice());
        parts.setStock(partsVO.getStock());
        parts.setImages(partsVO.getImages());
        parts.setStatus(1);
        parts.setSalesCount(0);
        
        this.save(parts);
    }
    
    @Override
    public IPage<Parts> getPartsList(Page<Parts> page, String category, String keyword) {
        LambdaQueryWrapper<Parts> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Parts::getStatus, 1);
        
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Parts::getCategory, category);
        }
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Parts::getName, keyword)
                    .or()
                    .like(Parts::getDescription, keyword));
        }
        
        wrapper.orderByDesc(Parts::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    public Parts getPartsDetail(Long id) {
        Parts parts = this.getById(id);
        if (parts == null) {
            throw new BusinessException("零部件不存在");
        }
        return parts;
    }
}
