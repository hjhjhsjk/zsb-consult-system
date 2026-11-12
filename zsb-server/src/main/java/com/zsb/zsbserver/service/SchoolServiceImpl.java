package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.School;
import com.zsb.zsbserver.mapper.SchoolMapper;
import com.zsb.zsbserver.vo.SchoolDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolServiceImpl implements SchoolService {

    @Autowired
    private SchoolMapper schoolMapper;

    @Override
    public List<School> getSchoolsByProvinceAndType(Integer provinceId, Integer type) {
        return schoolMapper.findByProvinceAndType(provinceId, type);
    }

    @Override
    public SchoolDetailVO getDetail(Integer id) {
        SchoolDetailVO vo = schoolMapper.findDetailById(id);
        if (vo == null) {
            throw new IllegalArgumentException("院校不存在");
        }
        return vo;
    }

    @Override
    public void addSchool(School school) {
        validate(school);
        // logo_url 是 NOT NULL 列，前端没传时补空串，避免 SQL 报错
        if (school.getLogoUrl() == null || school.getLogoUrl().trim().isEmpty()) {
            school.setLogoUrl("");
        }
        if (school.getCityName() == null) {
            school.setCityName("");
        }
        school.setCityId(school.getCityId() == null ? 0 : school.getCityId());
        schoolMapper.insertSchool(school);
    }

    @Override
    public void updateSchool(School school) {
        if (school.getId() == null) {
            throw new IllegalArgumentException("缺少院校 ID");
        }
        validate(school);
        if (school.getLogoUrl() == null) {
            school.setLogoUrl("");
        }
        if (school.getCityName() == null) {
            school.setCityName("");
        }
        school.setCityId(school.getCityId() == null ? 0 : school.getCityId());
        schoolMapper.updateSchool(school);
    }

    @Override
    public void deleteSchool(Integer id) {
        schoolMapper.deleteSchoolById(id);
    }

    private void validate(School school) {
        if (school.getSchoolName() == null || school.getSchoolName().trim().isEmpty()) {
            throw new IllegalArgumentException("院校名称不能为空");
        }
        if (school.getProvinceId() == null) {
            throw new IllegalArgumentException("请选择所属省份");
        }
        if (school.getSchoolType() == null || (school.getSchoolType() != 1 && school.getSchoolType() != 2)) {
            throw new IllegalArgumentException("院校类型只能是 1（公办）或 2（民办）");
        }
        school.setSchoolName(school.getSchoolName().trim());
    }
}
