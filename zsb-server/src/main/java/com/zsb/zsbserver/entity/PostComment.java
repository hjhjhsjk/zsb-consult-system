package com.zsb.zsbserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 帖子评论（对应表 post_comment） */
@Data
public class PostComment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Integer likeCount;
    /** 1=正常 0=已删除 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
