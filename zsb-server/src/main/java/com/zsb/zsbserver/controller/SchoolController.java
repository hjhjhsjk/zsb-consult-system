package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.entity.School;
import com.zsb.zsbserver.service.SchoolService;
import com.zsb.zsbserver.vo.SchoolDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/home")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    /** C-01 院校列表：type 1=公办 2=民办 */
    @GetMapping("/Schools/{provinceId}/{type}")
    public Result<List<School>> getSchools(@PathVariable Integer provinceId, @PathVariable Integer type) {
        return Result.success(schoolService.getSchoolsByProvinceAndType(provinceId, type));
    }

    /**
     * M-1 院校详情
     * 路径用小写 schools，避免和上面的 /Schools/{provinceId}/{type} 打架（Spring 路径区分大小写）。
     */
    @GetMapping("/schools/{id}")
    public Result<SchoolDetailVO> getSchoolDetail(@PathVariable Integer id) {
        return Result.success(schoolService.getDetail(id));
    }
}
