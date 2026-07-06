package com.efh.knowledge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.common.utils.UserContextUtil;
import com.efh.knowledge.entity.KnowledgeDoc;
import com.efh.knowledge.service.KnowledgeDocService;
import com.efh.knowledge.vo.DocSaveVO;
import com.efh.knowledge.vo.KnowledgeDocVO;
import com.efh.knowledge.vo.PayPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeDocController {

    @Autowired
    private KnowledgeDocService knowledgeDocService;

    @GetMapping("/doc/list")
    public Result<IPage<KnowledgeDocVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "12") Integer size,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String keyword,
                                            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        return Result.success(knowledgeDocService.listPublished(new Page<>(page, size), category, keyword, userId));
    }

    @GetMapping("/doc/categories")
    public Result<List<String>> categories() {
        return Result.success(knowledgeDocService.listCategories());
    }

    @GetMapping("/doc/{id}")
    public Result<KnowledgeDocVO> detail(@PathVariable Long id,
                                         @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        return Result.success(knowledgeDocService.getDetail(id, userId));
    }

    @PostMapping("/doc/{id}/unlock/points")
    public Result<?> unlockPoints(@PathVariable Long id,
                                  @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        knowledgeDocService.unlockByPoints(userId, id);
        return Result.success();
    }

    @PostMapping("/doc/{id}/unlock/alipay")
    public Result<PayPageVO> unlockAlipay(@PathVariable Long id,
                                          @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        return Result.success(knowledgeDocService.createAlipayUnlock(userId, id));
    }

    @GetMapping("/doc/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) throws Exception {
        Long userId = UserContextUtil.requireUserId(userIdHeader);
        KnowledgeDoc doc = knowledgeDocService.getById(id);
        Resource resource = knowledgeDocService.download(id, userId);
        String filename = URLEncoder.encode(doc.getFileName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/pay/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });
        return knowledgeDocService.handleAlipayNotify(params);
    }
}
