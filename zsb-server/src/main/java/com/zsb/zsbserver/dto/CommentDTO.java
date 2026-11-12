package com.zsb.zsbserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** 发表评论入参：{ "post_id": 9001, "content": "..." } */
@Data
public class CommentDTO {

    @JsonProperty("post_id")
    private Long postId;

    private String content;
}
