package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.AuthInterceptor;
import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.entity.*;
import com.zsb.zsbserver.mapper.StatsMapper;
import com.zsb.zsbserver.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 管理端接口。
 * 整个 /admin/** 在 AuthInterceptor 里已经强制校验 ustate == 3（管理员），
 * 所以这里不再重复判断权限。
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PlanService planService;

    @Autowired
    private FileStorageService fileStorageService;

    /** 单个只读聚合查询，没必要为它单独建 Service，直接注入 Mapper */
    @Autowired
    private StatsMapper statsMapper;

    // ================= 0. 仪表盘 / 通用上传 =================
    /** M-12 统计：用户 / 帖子 / 政策 / 院校 / 招生计划 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(statsMapper.stats());
    }

    /**
     * 通用图片上传：院校校徽、政策配图都走这里。
     * 返回可直接落库的 /uploads/xxx 相对路径。
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestPart("file") MultipartFile file,
                                 @RequestParam(value = "module", defaultValue = "common") String module) {
        return Result.success(fileStorageService.saveImage(file, module));
    }

    // ================= 1. 院校管理 =================
    @PostMapping("/school")
    public Result<String> addSchool(@RequestBody School school) {
        schoolService.addSchool(school);
        return Result.success("新增院校成功");
    }

    /** M-4 编辑院校 */
    @PutMapping("/school/{id}")
    public Result<String> updateSchool(@PathVariable Integer id, @RequestBody School school) {
        school.setId(id);
        schoolService.updateSchool(school);
        return Result.success("修改院校成功");
    }

    @DeleteMapping("/school/{id}")
    public Result<String> deleteSchool(@PathVariable Integer id) {
        schoolService.deleteSchool(id);
        return Result.success("删除院校成功");
    }

    // ================= 2. 用户管理 =================
    @GetMapping("/user/list")
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除用户成功");
    }

    /**
     * 调整用户角色：body 传 { "ustate": 3 } 设为管理员，{ "ustate": 0 } 降为学生。
     * 操作人 uid 从拦截器写入的 request attribute 取，不信任前端参数。
     */
    @PutMapping("/user/{id}/ustate")
    public Result<String> updateUserState(@PathVariable Long id,
                                          @RequestBody Map<String, Integer> body,
                                          HttpServletRequest request) {
        Long operatorUid = ((Number) request.getAttribute(AuthInterceptor.ATTR_UID)).longValue();
        userService.updateUserState(id, body.get("ustate"), operatorUid);
        return Result.success(body.get("ustate") == 3 ? "已设为管理员" : "已降为学生");
    }

    // ================= 3. 帖子管理 =================
    @GetMapping("/post/list")
    public Result<List<Post>> getAllPosts() {
        return Result.success(postService.getAllPosts());
    }

    /** M-10 删除帖子：软删除，仅把 status 改成 0 */
    @DeleteMapping("/post/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success("删除帖子成功");
    }

    // ================= 4. 专业管理 =================
    @GetMapping("/major/list")
    public Result<List<Major>> getAllMajors() {
        return Result.success(majorService.getAllMajors());
    }

    @PostMapping("/major")
    public Result<String> addMajor(@RequestBody Major major) {
        majorService.addMajor(major);
        return Result.success("新增专业成功");
    }

    @DeleteMapping("/major/{id}")
    public Result<String> deleteMajor(@PathVariable Integer id) {
        majorService.deleteMajor(id);
        return Result.success("删除专业成功");
    }

    // ================= 5. 政策管理 =================
    @GetMapping("/policy/list")
    public Result<List<Policy>> getAllPolicies() {
        return Result.success(policyService.getAllPolicies());
    }

    @PostMapping("/policy")
    public Result<String> addPolicy(@RequestBody Policy policy) {
        policyService.addPolicy(policy);
        return Result.success("发布政策成功");
    }

    /** M-8 编辑政策 */
    @PutMapping("/policy/{id}")
    public Result<String> updatePolicy(@PathVariable Long id, @RequestBody Policy policy) {
        policy.setId(id.intValue());
        policyService.updatePolicy(policy);
        return Result.success("修改政策成功");
    }

    @DeleteMapping("/policy/{id}")
    public Result<String> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return Result.success("删除政策成功");
    }

    // ================= 6. 招生计划管理（M-9） =================
    @GetMapping("/plan/list")
    public Result<List<RecruitmentPlan>> listPlans(@RequestParam(value = "schoolId", required = false) Integer schoolId) {
        return Result.success(schoolId == null ? planService.getAll() : planService.getBySchool(schoolId));
    }

    @PostMapping("/plan")
    public Result<String> addPlan(@RequestBody RecruitmentPlan plan) {
        planService.addPlan(plan);
        return Result.success("新增计划成功");
    }

    @PutMapping("/plan/{id}")
    public Result<String> updatePlan(@PathVariable Long id, @RequestBody RecruitmentPlan plan) {
        plan.setId(id);
        planService.updatePlan(plan);
        return Result.success("修改计划成功");
    }

    @DeleteMapping("/plan/{id}")
    public Result<String> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return Result.success("删除计划成功");
    }
}
