package com.zsb.zsbserver.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理：把各种异常统一翻译成 {code, msg, data}
 *
 * 分层原则：
 *   IllegalArgumentException → 400 业务校验失败（用户名已存在、帖子不存在…）
 *   参数类异常               → 400 前端传错（缺参、类型不对、JSON 格式错）
 *   其它 Exception           → 500 真崩了（数据库挂了…），日志打印堆栈
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleBiz(IllegalArgumentException ex) {
        return Result.fail(ex.getMessage());
    }

    /** 越权：HTTP 403，前端可以据此提示「无权操作」 */
    @ExceptionHandler(ForbiddenException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    public Result<String> handleForbidden(ForbiddenException ex) {
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.fail(msg);
    }

    /** 少了 @RequestParam 必填参数，例如发帖没传 content */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParam(MissingServletRequestParameterException ex) {
        return Result.fail("缺少必要参数：" + ex.getParameterName());
    }

    /** 参数类型不对，例如 /BBS/info/abc/0 把 abc 往 Integer 转 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return Result.fail("参数格式不正确：" + ex.getName());
    }

    /** 请求体不是合法 JSON */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleUnreadable(HttpMessageNotReadableException ex) {
        return Result.fail("请求体格式不正确");
    }

    /** 图片超过 5MB */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleUploadSize(MaxUploadSizeExceededException ex) {
        return Result.fail("图片不能超过 5MB");
    }

    /** 兜底：任何没被上面接住的异常，别让前端看到 500 空白页 */
    @ExceptionHandler(Exception.class)
    public Result<String> handleOther(Exception ex) {
        log.error("服务器内部错误", ex);
        return Result.error("服务器开小差了，请稍后重试");
    }
}
