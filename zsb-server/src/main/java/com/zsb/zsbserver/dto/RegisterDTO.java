package com.zsb.zsbserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册入参
 *
 * 前端实际传的是：{ uname, pwd, nickname, avatar, major, year, regions, phone }
 * 数据库/后端习惯叫：majorId、regionId。
 * 用 @JsonAlias 做「别名绑定」：JSON 里叫 major 也能塞进 majorId，
 * 这样前后端都不用改字段名，是最小改动方案。
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    private String uname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String pwd;

    private String nickname;

    private String avatar;

    /** 手机号：前端传 JSON 数字（13800138000 超出 int32），用 String 接再入库 BIGINT 最稳 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 前端字段名 major */
    @JsonAlias({"major"})
    private Integer majorId;

    private Integer year;

    /** 前端字段名 regions：用户选的最细一级地区 ID */
    @JsonAlias({"regions"})
    private Integer regionId;
}
