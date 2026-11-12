package com.zsb.zsbserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招生计划（对应表 recruitment_plan）
 *
 * schoolName / schoolLogo 是冗余字段：院校详情页刷新时只靠它兜底显示标题和校徽，
 * 所以新增计划时要把 school 表里的名称、校徽复制一份进来。
 */
@Data
public class RecruitmentPlan {
    private Long id;
    private Integer schoolId;
    private String schoolName;
    private String schoolLogo;
    private Integer majorId;
    private String majorName;
    private Integer year;
    private Integer planCount;
    private Integer tuitionFee;
    /** 1=普通 2=免试 */
    private Integer recruitmentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
