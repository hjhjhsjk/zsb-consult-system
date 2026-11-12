package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.entity.Major;
import com.zsb.zsbserver.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class MajorController {

    @Autowired
    private MajorService majorService;

    @GetMapping("/getMajor")
    public Result<List<Major>> getMajors() {
        return Result.success(majorService.getAllMajors());
    }
}