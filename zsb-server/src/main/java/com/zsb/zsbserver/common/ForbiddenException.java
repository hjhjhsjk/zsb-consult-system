package com.zsb.zsbserver.common;

/**
 * 越权操作（拿 A 的 token 去改 B 的数据）。
 * 单独定义异常类型，是为了让全局异常处理器把它翻译成 HTTP 403，
 * 而不是混在 400 业务错误里 —— 前端/网关靠状态码区分「参数错」和「没权限」。
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
