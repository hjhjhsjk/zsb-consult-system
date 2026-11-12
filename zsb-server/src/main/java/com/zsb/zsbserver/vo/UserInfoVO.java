package com.zsb.zsbserver.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用户信息 VO —— 字段名故意用下划线，因为前端读的就是 province_id / city_id / major。
 * JSON 里叫 major，数据库里叫 major_id，这层转换交给 VO，实体保持和表一致。
 */
@Data
public class UserInfoVO {
    private Long uid;
    private String uname;
    private String nickname;
    private String avatar;

    @JsonProperty("province_id")
    private Integer provinceId;

    @JsonProperty("city_id")
    private Integer cityId;

    @JsonProperty("major")
    private Integer majorId;

    private Integer year;

    /** 0=学生 3=管理员 */
    private Integer ustate;
}
