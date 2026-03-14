package com.efh.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子收藏实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_collection")
public class PostCollection extends BaseEntity {
    
    private Long postId;      // 帖子ID
    private Long userId;      // 用户ID
    private Integer status;   // 状态：1-已收藏，0-已取消
}
