package com.zsb.zsbserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子点赞（对应表 post_like）
 * 表上有 UNIQUE(user_id, post_id) 唯一索引，
 * 所以用 INSERT IGNORE 就能天然实现「重复点赞不报错」的幂等效果。
 */
@Data
public class PostLike {
    private Long id;
    private Long userId;
    private Long postId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
