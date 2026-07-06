package com.efh.knowledge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.common.utils.UserContextUtil;
import com.efh.knowledge.entity.KnowledgeDoc;
import com.efh.knowledge.service.KnowledgeDocService;
import com.efh.knowledge.vo.DocSaveVO;
import com.efh.knowledge.vo.KnowledgeDocVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/knowledge/admin")
public class KnowledgeAdminController {

    @Autowired
    private KnowledgeDocService knowledgeDocService;

    @GetMapping("/doc/list")
    public Result<IPage<KnowledgeDocVO>> list(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "20") Integer size,
                                              @RequestParam(required = false) Integer status) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(knowledgeDocService.adminList(userId, new Page<>(page, size), status));
    }

    @PostMapping(value = "/doc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<KnowledgeDoc> upload(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @RequestPart("file") MultipartFile file,
                                       @RequestPart("meta") DocSaveVO meta) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(knowledgeDocService.adminUpload(userId, file, meta));
    }

    @PutMapping("/doc/{id}")
    public Result<?> update(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                            @PathVariable Long id,
                            @RequestBody DocSaveVO meta) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        knowledgeDocService.adminUpdate(userId, id, meta);
        return Result.success();
    }

    @PostMapping("/doc/{id}/publish")
    public Result<?> publish(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                             @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        knowledgeDocService.adminPublish(userId, id);
        return Result.success();
    }

    @PostMapping("/doc/{id}/offline")
    public Result<?> offline(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                             @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        knowledgeDocService.adminOffline(userId, id);
        return Result.success();
    }

    @DeleteMapping("/doc/{id}")
    public Result<?> delete(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                            @PathVariable Long id) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        knowledgeDocService.adminDelete(userId, id);
        return Result.success();
    }
}
