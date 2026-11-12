package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.AuthInterceptor;
import com.zsb.zsbserver.common.ForbiddenException;
import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.dto.LoginDTO;
import com.zsb.zsbserver.dto.RegisterDTO;
import com.zsb.zsbserver.dto.UserUpdateDTO;
import com.zsb.zsbserver.service.UserService;
import com.zsb.zsbserver.service.FileStorageService;
import com.zsb.zsbserver.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    /** A-02 注册 */
    @PostMapping("/reg")
    public Result<String> register(@Validated @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    /**
     * 注册页头像上传：游客可用（白名单放行），因为此时还没有账号和 token。
     * 复用 saveImage 的三重校验（扩展名 / Content-Type / 大小），只收图片。
     * 返回 /uploads/avatar/... 相对路径，注册时随 JSON 一起提交。
     * 注：用户放弃注册会留下孤儿文件，量小可接受；后续可加定时清理。
     */
    @PostMapping("/reg/avatar")
    public Result<String> uploadRegAvatar(@RequestPart("file") MultipartFile file) {
        return Result.success(fileStorageService.saveImage(file, "avatar"));
    }

    /** A-01 登录：返回 { token, uid, ustate, nickname } */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    /**
     * B-01 获取用户信息
     * 只能查自己：token 里的 uid 必须等于路径里的 uid，否则 403。
     * 不然把 1、2、3 挨个试一遍就能把所有用户资料拿走。
     */
    @GetMapping("/info/{uid}")
    public Result<UserInfoVO> getUserInfo(@PathVariable Long uid, HttpServletRequest request) {
        if (!uid.equals(currentUid(request))) {
            throw new ForbiddenException("只能查看自己的信息");
        }
        return Result.success(userService.getUserInfo(uid));
    }

    /** M-13 修改个人信息：uid 一律从 token 取，前端传什么都不算 */
    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody UserUpdateDTO dto, HttpServletRequest request) {
        userService.updateUser(currentUid(request), dto);
        return Result.success("修改成功");
    }

    /** 上传头像：登录用户才能传，返回 /uploads/avatar/... 相对地址 */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return Result.success(fileStorageService.saveImage(file, "avatar"));
    }

    private Long currentUid(HttpServletRequest request) {
        Long uid = (Long) request.getAttribute(AuthInterceptor.ATTR_UID);
        if (uid == null) {
            throw new ForbiddenException("登录状态无效");
        }
        return uid;
    }
}
