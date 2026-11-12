package com.zsb.zsbserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * 修改个人信息入参（M-13）
 * 只允许改这几个「非敏感」字段：昵称、头像、意向专业、报考年份、所在地区。
 * 用户名不可改，密码走单独接口，避免越权。
 */
@Data
public class UserUpdateDTO {
    private String nickname;
    private String avatar;

    @JsonAlias({"major"})
    private Integer majorId;

    private Integer year;

    /** 前端字段名 regions */
    @JsonAlias({"regions"})
    private Integer regionId;
}
