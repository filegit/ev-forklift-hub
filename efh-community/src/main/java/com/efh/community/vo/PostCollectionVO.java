package com.efh.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostCollectionVO {
    private Long id;
    private Long postId;
    private String postTitle;
    private String postContent;
    private LocalDateTime createTime;
}
