package com.zsb.zsbserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 评论点赞（对应表 comment_like），唯一索引 UNIQUE(user_id, comment_id) */
@Data
public class CommentLike {
    private Long id;
    private Long userId;
    private Long commentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
