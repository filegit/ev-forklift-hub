package com.efh.parts.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.common.utils.UserContextUtil;
import com.efh.parts.entity.Parts;
import com.efh.parts.service.PartsService;
import com.efh.parts.vo.PartsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parts")
public class PartsController {

    @Autowired
    private PartsService partsService;

    @PostMapping
    public Result<?> publishParts(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                  @RequestBody PartsVO partsVO) {
        Long userId = com.efh.common.utils.UserContextUtil.requireUserId(userIdHeader);
        partsService.publishParts(userId, partsVO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<IPage<Parts>> getPartsList(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "12") Integer size,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String keyword) {
        IPage<Parts> partsPage = partsService.getPartsList(new Page<>(page, size), category, keyword);
        return Result.success(partsPage);
    }

    @GetMapping("/{id}")
    public Result<Parts> getPartsDetail(@PathVariable Long id) {
        return Result.success(partsService.getPartsDetail(id));
    }
}
