package com.efh.community.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 评论请求 VO
 */
@Data
public class CommentVO {
    
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private Long parentId;  // 父评论ID，0或null表示一级评论
}
