package com.efh.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 帖子实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_0")  // 暂时使用单表
public class Post extends BaseEntity {
    
    private Long userId;          // 发帖用户ID
    private String title;         // 标题
    private String content;       // 内容
    private Integer category;     // 分类：1-技术交流 2-故障求助 3-经验分享 4-其他
    private Integer viewCount;    // 浏览量
    private Integer likeCount;    // 点赞数
    private Integer commentCount; // 评论数
    private Integer isTop;        // 是否置顶：0-否 1-是
    private LocalDateTime topTime; // 置顶时间
    private Integer status;       // 状态：0-待审核 1-已发布 2-已删除
}
