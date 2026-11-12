package com.zsb.zsbserver.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    // 1. 成功
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    // 2. 业务失败（比如参数校验不通过、密码错误）—— 统一用 400
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(400); // 规范：400 代表客户端输入有误
        result.setMsg(message);
        result.setData(null);
        return result;
    }

    // 3. 系统异常（真正的服务器崩溃，比如数据库挂了）—— 用 500
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(message);
        result.setData(null);
        return result;
    }
}