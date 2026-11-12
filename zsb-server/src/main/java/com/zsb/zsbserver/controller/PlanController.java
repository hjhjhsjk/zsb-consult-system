package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.entity.RecruitmentPlan;
import com.zsb.zsbserver.service.PlanService;
import com.zsb.zsbserver.vo.SearchPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 招生计划 + 全局搜索：都挂在 /home 下，对前端来说属于「首页数据」 */
@RestController
@RequestMapping("/home")
public class PlanController {

    @Autowired
    private PlanService planService;

    /** E-01 某院校的招生计划（院校详情页） */
    @GetMapping("/getRecruitmentPlan/{schoolId}")
    public Result<List<RecruitmentPlan>> getRecruitmentPlan(@PathVariable Integer schoolId) {
        return Result.success(planService.getBySchool(schoolId));
    }

    /**
     * F-01 全局搜索：keyword 必传，region_id 可选。
     * 返回的 id 是院校 ID，前端拿它跳 /school/{id}。
     */
    @GetMapping("/search")
    public Result<List<SearchPlanVO>> search(@RequestParam("keyword") String keyword,
                                             @RequestParam(value = "region_id", required = false) Integer regionId) {
        return Result.success(planService.search(keyword, regionId));
    }
}
