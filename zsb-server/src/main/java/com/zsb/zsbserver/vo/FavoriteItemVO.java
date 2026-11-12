package com.zsb.zsbserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏项 VO
 *
 * 关键点：id 必须等于 target_id（被收藏的帖子/政策 ID），不能是收藏记录自己的主键。
 * 因为前端 `goPost(item.id)` 直接拿它跳详情，返回收藏记录 id 会打开错误的帖子。
 */
@Data
public class FavoriteItemVO {
    /** = targetId，前端跳转用 */
    private Long id;

    @JsonProperty("target_id")
    private Long targetId;

    @JsonProperty("target_type")
    private Integer targetType;

    /** 帖子内容（type=1 时有值） */
    private String content;

    /** 政策标题（type=2 时有值） */
    private String title;

    @JsonProperty("image_url")
    private String imageUrl;

    /** 帖子时间（type=1） */
    @JsonProperty("post_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime postTime;

    /** 政策时间（type=2） */
    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
