package com.efh.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment_0")
public class Comment extends BaseEntity {
    
    private Long postId;      // 帖子ID
    private Long userId;      // 评论用户ID
    private Long parentId;    // 父评论ID，0表示一级评论
    private String content;   // 评论内容
    private Integer likeCount; // 点赞数
}
