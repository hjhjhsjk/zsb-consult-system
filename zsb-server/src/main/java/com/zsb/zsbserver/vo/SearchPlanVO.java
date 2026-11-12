package com.zsb.zsbserver.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 搜索结果 VO
 *
 * 注意 id 是「院校 ID」而不是招生计划行 ID —— 前端点结果跳的是 /school/{id}。
 * schoolType 在 SQL 里已转成中文（公办/民办），前端直接当标签文字显示。
 */
@Data
public class SearchPlanVO {
    private Integer id;

    @JsonProperty("school_name")
    private String schoolName;

    @JsonProperty("school_logo")
    private String schoolLogo;

    @JsonProperty("major_name")
    private String majorName;

    @JsonProperty("tuition_fee")
    private Integer tuitionFee;

    @JsonProperty("plan_count")
    private Integer planCount;

    private Integer year;

    /** 公办 / 民办 */
    @JsonProperty("school_type")
    private String schoolType;
}
