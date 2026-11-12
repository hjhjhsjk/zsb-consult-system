package com.zsb.zsbserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/** 评论 VO（前端读 nickname / content / create_time / like_count / is_liked） */
@Data
public class CommentVO {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private String nickname;
    private String avatar;
    private String content;

    @JsonProperty("like_count")
    private Integer likeCount;

    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonProperty("is_liked")
    private Integer isLiked;
}
