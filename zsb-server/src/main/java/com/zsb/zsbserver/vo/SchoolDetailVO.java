package com.zsb.zsbserver.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 院校详情 VO
 * logo_url 与 school_logo 同时返回：前端两处代码分别用了这两个名字，双返回最省事。
 */
@Data
public class SchoolDetailVO {
    private Integer id;

    @JsonProperty("school_name")
    private String schoolName;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("school_logo")
    private String schoolLogo;

    @JsonProperty("city_name")
    private String cityName;

    /** 1=公办 2=民办 */
    @JsonProperty("school_type")
    private Integer schoolType;

    @JsonProperty("province_id")
    private Integer provinceId;
}
