package com.efh.knowledge.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.common.result.Result;
import com.efh.knowledge.config.AlipayPayProperties;
import com.efh.knowledge.constant.KnowledgeConstants;
import com.efh.knowledge.entity.KnowledgeDoc;
import com.efh.knowledge.entity.KnowledgeUnlock;
import com.efh.knowledge.feign.UserFeignClient;
import com.efh.knowledge.mapper.KnowledgeDocMapper;
import com.efh.knowledge.mapper.KnowledgeUnlockMapper;
import com.efh.knowledge.vo.DocSaveVO;
import com.efh.knowledge.vo.KnowledgeDocVO;
import com.efh.knowledge.vo.PayPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeDocService extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> {

    private static final Set<String> SUCCESS_STATUS = new HashSet<>(Arrays.asList("TRADE_SUCCESS", "TRADE_FINISHED"));

    @Autowired
    private KnowledgeUnlockMapper unlockMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private AlipayPayProperties alipayProperties;

    public IPage<KnowledgeDocVO> listPublished(Page<KnowledgeDoc> page, String category, String keyword, Long userId) {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, KnowledgeConstants.STATUS_PUBLISHED);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(KnowledgeDoc::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(KnowledgeDoc::getTitle, keyword).or().like(KnowledgeDoc::getSummary, keyword));
        }
        wrapper.orderByDesc(KnowledgeDoc::getCreateTime);
        IPage<KnowledgeDoc> docPage = page(page, wrapper);
        Set<Long> unlockedIds = userId == null ? Collections.emptySet() : loadUnlockedDocIds(userId);
        return docPage.convert(doc -> toVO(doc, unlockedIds.contains(doc.getId())));
    }

    public KnowledgeDocVO getDetail(Long docId, Long userId) {
        KnowledgeDoc doc = requirePublished(docId);
        doc.setViewCount((doc.getViewCount() == null ? 0 : doc.getViewCount()) + 1);
        updateById(doc);
        boolean unlocked = isUnlocked(userId, doc);
        return toVO(doc, unlocked);
    }

    public Resource download(Long docId, Long userId) {
        KnowledgeDoc doc = requirePublished(docId);
        if (!isUnlocked(userId, doc)) {
            throw new BusinessException(403, "请先解锁该文档");
        }
        Path path = fileStorageService.resolve(doc.getFilePath());
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在");
        }
        doc.setDownloadCount((doc.getDownloadCount() == null ? 0 : doc.getDownloadCount()) + 1);
        updateById(doc);
        return new FileSystemResource(path.toFile());
    }

    @Transactional(rollbackFor = Exception.class)
    public void unlockByPoints(Long userId, Long docId) {
        KnowledgeDoc doc = requirePublished(docId);
        validatePointsAccess(doc);
        if (isUnlocked(userId, doc)) {
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("points", doc.getPointsPrice());
        body.put("reason", "解锁知识库文档:" + doc.getTitle());
        Result<Void> result = userFeignClient.consumePoints(String.valueOf(userId), body);
        if (result == null || result.getCode() != 200) {
            throw new BusinessException(result != null ? result.getMessage() : "积分扣减失败");
        }
        saveUnlock(userId, doc, KnowledgeConstants.UNLOCK_POINTS, doc.getPointsPrice(), BigDecimal.ZERO, null, null);
    }

    public PayPageVO createAlipayUnlock(Long userId, Long docId) {
        KnowledgeDoc doc = requirePublished(docId);
        validatePaidAccess(doc);
        if (isUnlocked(userId, doc)) {
            throw new BusinessException("文档已解锁");
        }
        if (!alipayProperties.isConfigured()) {
            throw new BusinessException("支付宝未配置，无法现金解锁");
        }
        KnowledgeUnlock pending = unlockMapper.selectOne(new LambdaQueryWrapper<KnowledgeUnlock>()
                .eq(KnowledgeUnlock::getUserId, userId)
                .eq(KnowledgeUnlock::getDocId, docId)
                .eq(KnowledgeUnlock::getUnlockType, KnowledgeConstants.UNLOCK_ALIPAY)
                .isNull(KnowledgeUnlock::getThirdTradeNo));
        String payNo = pending != null ? pending.getPayNo() : "KBPAY" + IdUtil.getSnowflakeNextIdStr();
        try {
            AlipayClient client = new DefaultAlipayClient(
                    alipayProperties.getGatewayUrl(),
                    alipayProperties.getAppId(),
                    alipayProperties.getPrivateKey(),
                    "json", "UTF-8",
                    alipayProperties.getAlipayPublicKey(),
                    "RSA2"
            );
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl() + "?payNo=" + payNo + "&docId=" + docId);
            request.setBizContent(String.format(
                    "{\"out_trade_no\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\","
                            + "\"total_amount\":\"%s\",\"subject\":\"%s\"}",
                    payNo, doc.getMoneyPrice().toPlainString(), "知识库-" + doc.getTitle()
            ));
            PayPageVO vo = new PayPageVO();
            vo.setPayNo(payNo);
            vo.setPayForm(client.pageExecute(request).getBody());
            if (pending == null) {
                KnowledgeUnlock record = new KnowledgeUnlock();
                record.setUserId(userId);
                record.setDocId(docId);
                record.setUnlockType(KnowledgeConstants.UNLOCK_ALIPAY);
                record.setPointsCost(0);
                record.setMoneyPaid(doc.getMoneyPrice());
                record.setPayNo(payNo);
                unlockMapper.insert(record);
            }
            return vo;
        } catch (AlipayApiException e) {
            throw new BusinessException("创建支付失败: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        try {
            if (!AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), "UTF-8", "RSA2")) {
                return "failure";
            }
        } catch (AlipayApiException e) {
            return "failure";
        }
        if (!SUCCESS_STATUS.contains(params.get("trade_status"))) {
            return "success";
        }
        String payNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        KnowledgeUnlock unlock = unlockMapper.selectOne(new LambdaQueryWrapper<KnowledgeUnlock>()
                .eq(KnowledgeUnlock::getPayNo, payNo)
                .eq(KnowledgeUnlock::getUnlockType, KnowledgeConstants.UNLOCK_ALIPAY));
        if (unlock == null) {
            return "failure";
        }
        if (unlock.getThirdTradeNo() != null && !unlock.getThirdTradeNo().isEmpty()) {
            return "success";
        }
        unlock.setThirdTradeNo(tradeNo);
        unlockMapper.updateById(unlock);
        log.info("知识库支付成功 payNo={} docId={} userId={}", payNo, unlock.getDocId(), unlock.getUserId());
        return "success";
    }

    public boolean isUnlocked(Long userId, KnowledgeDoc doc) {
        if (doc.getAccessType() == KnowledgeConstants.ACCESS_FREE) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        try {
            if (adminAuthService.getUserBrief(userId).getUserType() != null
                    && adminAuthService.getUserBrief(userId).getUserType() == KnowledgeConstants.USER_TYPE_ADMIN) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return hasValidUnlock(userId, doc.getId());
    }

    private boolean hasValidUnlock(Long userId, Long docId) {
        KnowledgeUnlock record = unlockMapper.selectOne(new LambdaQueryWrapper<KnowledgeUnlock>()
                .eq(KnowledgeUnlock::getUserId, userId)
                .eq(KnowledgeUnlock::getDocId, docId)
                .last("LIMIT 1"));
        if (record == null) {
            return false;
        }
        if (KnowledgeConstants.UNLOCK_ALIPAY.equals(record.getUnlockType())) {
            return record.getThirdTradeNo() != null && !record.getThirdTradeNo().isEmpty();
        }
        return true;
    }

    public KnowledgeDoc adminUpload(Long adminId, MultipartFile file, DocSaveVO vo) {
        adminAuthService.requireAdmin(adminId);
        validateSaveVO(vo);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传文档文件");
        }
        String stored = fileStorageService.store(file);
        KnowledgeDoc doc = buildDoc(vo, adminId);
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(stored);
        doc.setFileSize(file.getSize());
        doc.setFileType(extractType(file.getOriginalFilename()));
        doc.setStatus(KnowledgeConstants.STATUS_DRAFT);
        doc.setDownloadCount(0);
        doc.setViewCount(0);
        save(doc);
        return doc;
    }

    public void adminUpdate(Long adminId, Long docId, DocSaveVO vo) {
        adminAuthService.requireAdmin(adminId);
        validateSaveVO(vo);
        KnowledgeDoc doc = getById(docId);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        doc.setTitle(vo.getTitle());
        doc.setSummary(vo.getSummary());
        doc.setCategory(vo.getCategory());
        doc.setAccessType(vo.getAccessType());
        doc.setPointsPrice(defaultInt(vo.getPointsPrice()));
        doc.setMoneyPrice(defaultMoney(vo.getMoneyPrice()));
        updateById(doc);
    }

    public void adminPublish(Long adminId, Long docId) {
        adminAuthService.requireAdmin(adminId);
        KnowledgeDoc doc = getById(docId);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        doc.setStatus(KnowledgeConstants.STATUS_PUBLISHED);
        updateById(doc);
    }

    public void adminOffline(Long adminId, Long docId) {
        adminAuthService.requireAdmin(adminId);
        KnowledgeDoc doc = getById(docId);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        doc.setStatus(KnowledgeConstants.STATUS_OFFLINE);
        updateById(doc);
    }

    public void adminDelete(Long adminId, Long docId) {
        adminAuthService.requireAdmin(adminId);
        KnowledgeDoc doc = getById(docId);
        if (doc == null) {
            return;
        }
        fileStorageService.delete(doc.getFilePath());
        removeById(docId);
        unlockMapper.delete(new LambdaQueryWrapper<KnowledgeUnlock>().eq(KnowledgeUnlock::getDocId, docId));
    }

    public IPage<KnowledgeDocVO> adminList(Long adminId, Page<KnowledgeDoc> page, Integer status) {
        adminAuthService.requireAdmin(adminId);
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(KnowledgeDoc::getStatus, status);
        }
        wrapper.orderByDesc(KnowledgeDoc::getCreateTime);
        return page(page, wrapper).convert(doc -> toVO(doc, true));
    }

    public List<String> listCategories() {
        return Arrays.asList("维修手册", "技术规范", "培训资料", "安全指南", "其他");
    }

    private KnowledgeDoc requirePublished(Long docId) {
        KnowledgeDoc doc = getById(docId);
        if (doc == null || doc.getStatus() != KnowledgeConstants.STATUS_PUBLISHED) {
            throw new BusinessException("文档不存在或未发布");
        }
        return doc;
    }

    private void saveUnlock(Long userId, KnowledgeDoc doc, String type, Integer points, BigDecimal money, String payNo, String tradeNo) {
        KnowledgeUnlock unlock = new KnowledgeUnlock();
        unlock.setUserId(userId);
        unlock.setDocId(doc.getId());
        unlock.setUnlockType(type);
        unlock.setPointsCost(points);
        unlock.setMoneyPaid(money);
        unlock.setPayNo(payNo);
        unlock.setThirdTradeNo(tradeNo);
        unlockMapper.insert(unlock);
    }

    private Set<Long> loadUnlockedDocIds(Long userId) {
        return unlockMapper.selectList(new LambdaQueryWrapper<KnowledgeUnlock>()
                        .eq(KnowledgeUnlock::getUserId, userId))
                .stream()
                .filter(u -> !KnowledgeConstants.UNLOCK_ALIPAY.equals(u.getUnlockType())
                        || (u.getThirdTradeNo() != null && !u.getThirdTradeNo().isEmpty()))
                .map(KnowledgeUnlock::getDocId)
                .collect(Collectors.toSet());
    }

    private KnowledgeDoc buildDoc(DocSaveVO vo, Long adminId) {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(vo.getTitle());
        doc.setSummary(vo.getSummary());
        doc.setCategory(vo.getCategory());
        doc.setAccessType(vo.getAccessType());
        doc.setPointsPrice(defaultInt(vo.getPointsPrice()));
        doc.setMoneyPrice(defaultMoney(vo.getMoneyPrice()));
        doc.setCreatedBy(adminId);
        return doc;
    }

    private void validateSaveVO(DocSaveVO vo) {
        if (vo.getTitle() == null || vo.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        if (vo.getAccessType() == null) {
            throw new BusinessException("请选择访问方式");
        }
        if (vo.getAccessType() == KnowledgeConstants.ACCESS_POINTS && (vo.getPointsPrice() == null || vo.getPointsPrice() <= 0)) {
            throw new BusinessException("请设置积分价格");
        }
        if (vo.getAccessType() == KnowledgeConstants.ACCESS_PAID && (vo.getMoneyPrice() == null || vo.getMoneyPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BusinessException("请设置现金价格");
        }
        if (vo.getAccessType() == KnowledgeConstants.ACCESS_BOTH) {
            if (vo.getPointsPrice() == null || vo.getPointsPrice() <= 0) {
                throw new BusinessException("请设置积分价格");
            }
            if (vo.getMoneyPrice() == null || vo.getMoneyPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("请设置现金价格");
            }
        }
    }

    private void validatePointsAccess(KnowledgeDoc doc) {
        if (doc.getAccessType() != KnowledgeConstants.ACCESS_POINTS && doc.getAccessType() != KnowledgeConstants.ACCESS_BOTH) {
            throw new BusinessException("该文档不支持积分解锁");
        }
    }

    private void validatePaidAccess(KnowledgeDoc doc) {
        if (doc.getAccessType() != KnowledgeConstants.ACCESS_PAID && doc.getAccessType() != KnowledgeConstants.ACCESS_BOTH) {
            throw new BusinessException("该文档不支持现金解锁");
        }
    }

    private KnowledgeDocVO toVO(KnowledgeDoc doc, boolean unlocked) {
        KnowledgeDocVO vo = new KnowledgeDocVO();
        BeanUtils.copyProperties(doc, vo);
        vo.setUnlocked(unlocked || doc.getAccessType() == KnowledgeConstants.ACCESS_FREE);
        vo.setAccessLabel(buildAccessLabel(doc));
        return vo;
    }

    private String buildAccessLabel(KnowledgeDoc doc) {
        switch (doc.getAccessType()) {
            case KnowledgeConstants.ACCESS_FREE:
                return "免费";
            case KnowledgeConstants.ACCESS_POINTS:
                return doc.getPointsPrice() + " 积分";
            case KnowledgeConstants.ACCESS_PAID:
                return "¥" + doc.getMoneyPrice();
            case KnowledgeConstants.ACCESS_BOTH:
                return doc.getPointsPrice() + " 积分 / ¥" + doc.getMoneyPrice();
            default:
                return "未知";
        }
    }

    private Integer defaultInt(Integer v) {
        return v == null ? 0 : v;
    }

    private BigDecimal defaultMoney(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String extractType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "file";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
