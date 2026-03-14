package com.efh.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论点赞实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment_like")
public class CommentLike extends BaseEntity {
    
    private Long commentId;   // 评论ID
    private Long userId;      // 用户ID
    private Integer status;   // 状态：1-已赞，0-已取消
}
