package com.zsb.zsbserver.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zsb.zsbserver.entity.User;
import com.zsb.zsbserver.mapper.UserMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录鉴权拦截器：所有「需要登录」的接口都会先走这里。
 *
 * 判定流程：
 *   1. 预检请求（OPTIONS）直接放行 —— 浏览器跨域探测不带 token
 *   2. 白名单路径直接放行（登录、注册、字典、首页数据）
 *   3. 取 Authorization: Bearer <token>，没有 → 401
 *   4. 验签解出 uid，失败/过期 → 401
 *   5. /admin/** 额外要求 ustate == 3（管理员），否则 → 403
 *   6. 通过后把 uid 塞进 request attribute，Controller 直接取，避免信任前端传的 uid
 *
 * 注意：401 必须用 HTTP 状态码写出，不能只返回 {code:401}。
 * 因为前端 axios 拦截器判断的是 err.response.status === 401，业务码它看不到。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_UID = "uid";

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 1. 跨域预检直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // 2. 白名单：不需要登录就能访问的接口
        if (isWhiteList(path)) {
            // 带 token 时顺便解析出来，方便详情接口判断「我有没有点过赞」
            String token = resolveToken(request);
            if (token != null) {
                try {
                    request.setAttribute(ATTR_UID, jwtUtils.parseUid(token));
                } catch (JwtException ignored) {
                    // 白名单接口即使 token 过期也不报错，按游客处理
                }
            }
            return true;
        }

        // 3. 取 token
        String token = resolveToken(request);
        if (token == null) {
            writeError(response, 401, "请先登录");
            return false;
        }

        // 4. 验签
        Long uid;
        try {
            uid = jwtUtils.parseUid(token);
        } catch (JwtException | IllegalArgumentException e) {
            writeError(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // 5. 管理端接口要求管理员（ustate = 3）
        if (path.startsWith("/admin")) {
            User user = userMapper.findById(uid);
            if (user == null || user.getUstate() == null || user.getUstate() != 3) {
                writeError(response, 403, "无管理员权限");
                return false;
            }
        }

        request.setAttribute(ATTR_UID, uid);
        return true;
    }

    /** 白名单：登录/注册/字典/首页公开数据/论坛列表/静态资源 */
    private boolean isWhiteList(String path) {
        return path.equals("/user/login")
                || path.equals("/user/reg")
                || path.equals("/user/reg/avatar")   // 注册页选传头像，游客可用
                || path.equals("/user/getMajor")
                || path.startsWith("/user/regions/")
                || path.startsWith("/home/")
                || path.startsWith("/BBS/info/")
                || path.equals("/BBS/latest")
                || path.startsWith("/BBS/GetPostDetail/")
                || path.startsWith("/BBS/comments/")
                || path.startsWith("/uploads/")
                || path.startsWith("/error");
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private void writeError(HttpServletResponse response, int status, String msg) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(msg)));
    }
}
