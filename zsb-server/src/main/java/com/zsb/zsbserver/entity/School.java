package com.zsb.zsbserver.entity;

import lombok.Data;

/**
 * 院校（对应表 school）
 * cityName 是冗余字段，前端列表/详情直接展示，不用再联表查 region。
 */
@Data
public class School {
    private Integer id;
    private String schoolName;
    private String logoUrl;
    private String cityName;
    /** 1=公办 2=民办 */
    private Integer schoolType;
    private Integer provinceId;
    private Integer cityId;
}
