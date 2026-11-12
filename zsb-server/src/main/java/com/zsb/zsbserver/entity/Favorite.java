package com.zsb.zsbserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏（对应表 favorite）
 * 多态设计：targetType 1=帖子 2=政策，targetId 指向对应表的 id。
 * 因为目标表不确定，所以 favorite 不建外键，存在性要在代码里校验。
 */
@Data
public class Favorite {
    private Long id;
    private Long userId;
    /** 1=帖子 2=政策 */
    private Integer targetType;
    private Long targetId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
