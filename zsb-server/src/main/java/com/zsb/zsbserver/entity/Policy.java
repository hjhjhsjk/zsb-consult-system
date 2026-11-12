package com.zsb.zsbserver.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Policy {
    private Integer id;
    private String title;
    private String content;
    private Integer publisher;
    private Integer categoryId;
    private Integer provinceId;
    private Integer isTop;
    private Integer viewCount;

    // 文档要求返回 yyyy-MM-dd HH:mm:ss 格式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String url;

    /** 非表字段：当前登录用户是否已收藏（详情接口带出，列表为 null） */
    private Integer isFav;
}