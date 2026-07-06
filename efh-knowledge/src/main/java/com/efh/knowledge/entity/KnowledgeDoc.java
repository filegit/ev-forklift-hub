package com.efh.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_doc")
public class KnowledgeDoc extends BaseEntity {

    private String title;
    private String summary;
    /** 分类：维修手册/技术规范/培训资料/其他 */
    private String category;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    /** 0免费 1积分 2付费 3积分或付费 */
    private Integer accessType;
    private Integer pointsPrice;
    private BigDecimal moneyPrice;
    /** 0草稿 1已发布 2已下架 */
    private Integer status;
    private Integer downloadCount;
    private Integer viewCount;
    private Long createdBy;
}
