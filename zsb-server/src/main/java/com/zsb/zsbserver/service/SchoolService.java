package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.School;
import com.zsb.zsbserver.vo.SchoolDetailVO;

import java.util.List;

public interface SchoolService {

    List<School> getSchoolsByProvinceAndType(Integer provinceId, Integer type);

    /** 院校详情（logo_url 与 school_logo 双字段） */
    SchoolDetailVO getDetail(Integer id);

    void addSchool(School school);

    void updateSchool(School school);

    void deleteSchool(Integer id);
}
