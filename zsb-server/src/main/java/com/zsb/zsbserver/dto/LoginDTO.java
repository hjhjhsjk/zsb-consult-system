package com.zsb.zsbserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data; // 必须有这行

@Data // 必须有这行
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String uname;

    @NotBlank(message = "密码不能为空")
    private String pwd;
}