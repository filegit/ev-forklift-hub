package com.efh.parts.controller;

import com.baomidou.mybatisplus.core.metadata.  IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.common.utils.JwtUtil;
import com.efh.parts.entity.Parts;
import com.efh.parts.service.PartsService;
import com.efh.parts.vo.PartsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parts")
public class PartsController {
    
    @Autowired
    private PartsService partsService;
    
    /**
     * 发布零部件
     */
    @PostMapping
    public Result<?> publishParts(@RequestHeader("Authorization") String token,
                                  @RequestBody PartsVO partsVO) {
        Long userId = JwtUtil.getUserId(token);
        partsService.publishParts(userId, partsVO);
        return Result.success();
    }
    
    /**
     * 零部件列表
     */
    @GetMapping("/list")
    public Result<IPage<Parts>> getPartsList(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String keyword) {
        IPage<Parts> partsPage = partsService.getPartsList(new Page<>(page, size), category, keyword);
        return Result.success(partsPage);
    }
    
    /**
     * 零部件详情
     */
    @GetMapping("/{id}")
    public Result<Parts> getPartsDetail(@PathVariable Long id) {
        Parts parts = partsService.getPartsDetail(id);
        return Result.success(parts);
    }
}
