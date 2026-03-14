package com.efh.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子点赞实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_like")
public class PostLike extends BaseEntity {
    
    private Long postId;      // 帖子ID
    private Long userId;      // 用户ID
    private Integer status;   // 状态：1-已赞，0-已取消
}
