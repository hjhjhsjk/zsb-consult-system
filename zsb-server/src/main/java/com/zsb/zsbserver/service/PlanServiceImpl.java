package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.RecruitmentPlan;
import com.zsb.zsbserver.entity.School;
import com.zsb.zsbserver.mapper.RecruitmentPlanMapper;
import com.zsb.zsbserver.mapper.SchoolMapper;
import com.zsb.zsbserver.vo.SearchPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {

    @Autowired
    private RecruitmentPlanMapper planMapper;

    @Autowired
    private SchoolMapper schoolMapper;

    @Override
    public List<RecruitmentPlan> getBySchool(Integer schoolId) {
        return planMapper.findBySchoolId(schoolId);
    }

    @Override
    public List<RecruitmentPlan> getAll() {
        return planMapper.findAll();
    }

    @Override
    public List<SearchPlanVO> search(String keyword, Integer regionId) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return Collections.emptyList();
        }
        return planMapper.search(kw, regionId);
    }

    @Override
    public void addPlan(RecruitmentPlan plan) {
        validate(plan);
        // 冗余字段：院校名和校徽从 school 表复制一份，
        // 这样院校详情页刷新时就算不联表也能显示标题和 logo
        fillSchoolSnapshot(plan);
        planMapper.insert(plan);
    }

    @Override
    public void updatePlan(RecruitmentPlan plan) {
        if (plan.getId() == null) {
            throw new IllegalArgumentException("缺少计划 ID");
        }
        validate(plan);
        fillSchoolSnapshot(plan);
        planMapper.update(plan);
    }

    @Override
    public void deletePlan(Long id) {
        planMapper.deleteById(id);
    }

    private void validate(RecruitmentPlan plan) {
        if (plan.getSchoolId() == null) {
            throw new IllegalArgumentException("请选择院校");
        }
        if (plan.getMajorName() == null || plan.getMajorName().trim().isEmpty()) {
            throw new IllegalArgumentException("专业名称不能为空");
        }
        if (plan.getYear() == null) {
            throw new IllegalArgumentException("招生年份不能为空");
        }
        plan.setMajorName(plan.getMajorName().trim());
        plan.setPlanCount(plan.getPlanCount() == null ? 0 : plan.getPlanCount());
        plan.setTuitionFee(plan.getTuitionFee() == null ? 0 : plan.getTuitionFee());
        plan.setRecruitmentType(plan.getRecruitmentType() == null ? 1 : plan.getRecruitmentType());
    }

    private void fillSchoolSnapshot(RecruitmentPlan plan) {
        School school = schoolMapper.findById(plan.getSchoolId());
        if (school == null) {
            throw new IllegalArgumentException("院校不存在");
        }
        plan.setSchoolName(school.getSchoolName());
        plan.setSchoolLogo(school.getLogoUrl() == null ? "" : school.getLogoUrl());
    }
}
