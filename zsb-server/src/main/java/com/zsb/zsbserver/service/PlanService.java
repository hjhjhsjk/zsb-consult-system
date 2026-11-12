package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.RecruitmentPlan;
import com.zsb.zsbserver.vo.SearchPlanVO;

import java.util.List;

public interface PlanService {

    /** 院校详情/管理端：某校全部招生计划 */
    List<RecruitmentPlan> getBySchool(Integer schoolId);

    /** 管理端不选学校时查全部 */
    List<RecruitmentPlan> getAll();

    /** 全局搜索（院校名 / 专业名） */
    List<SearchPlanVO> search(String keyword, Integer regionId);

    void addPlan(RecruitmentPlan plan);

    void updatePlan(RecruitmentPlan plan);

    void deletePlan(Long id);
}
