package com.efh.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_unlock")
public class KnowledgeUnlock extends BaseEntity {

    private Long userId;
    private Long docId;
    /** free / points / alipay */
    private String unlockType;
    private Integer pointsCost;
    private BigDecimal moneyPaid;
    private String payNo;
    private String thirdTradeNo;
}
