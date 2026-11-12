package com.zsb.zsbserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子（对应表 post）
 *
 * 字段说明：
 *   postCount 评论数、favCount 收藏数、likeCount 点赞数 —— 都是冗余计数，由互动接口维护
 *   status    1=正常 0=已删除（软删除，管理端删帖只改这个字段）
 *   imageUrl  配图地址，空串表示无图
 */
@Data
public class Post {
    private Long id;
    private Long userId;
    private String content;
    private String imageUrl;
    private Integer regionId;
    private Integer schoolId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favCount;
    private Integer postCount;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime postTime;

    /**
     * 下面两个不是 post 表的列，是列表查询时 JOIN user 带出来的作者信息。
     * 放在这里是为了让前端列表直接有昵称/头像，不用再按 userId 逐个查。
     */
    private String nickname;
    private String avatar;
}
