package com.zsb.zsbserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子详情 VO
 *
 * 相比列表多三样东西：
 *   1. 作者昵称 nickname（列表页不需要）
 *   2. is_liked / is_fav 当前用户是否点过赞、收藏过（1/0）
 *   3. fav_count 收藏数
 * 注意时间字段叫 post_time（和列表一致），评论的时间叫 create_time，别混。
 */
@Data
public class PostDetailVO {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private String nickname;
    private String avatar;
    private String content;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("region_id")
    private Integer regionId;

    @JsonProperty("school_id")
    private Integer schoolId;

    @JsonProperty("view_count")
    private Integer viewCount;

    @JsonProperty("like_count")
    private Integer likeCount;

    @JsonProperty("fav_count")
    private Integer favCount;

    @JsonProperty("post_count")
    private Integer postCount;

    @JsonProperty("post_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime postTime;

    /** 当前用户是否已点赞：1 是 0 否 */
    @JsonProperty("is_liked")
    private Integer isLiked;

    /** 当前用户是否已收藏：1 是 0 否 */
    @JsonProperty("is_fav")
    private Integer isFav;
}
