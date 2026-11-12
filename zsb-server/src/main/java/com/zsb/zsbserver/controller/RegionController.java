package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.entity.Region;
import com.zsb.zsbserver.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping("/regions/{parentId}")
    public Result<List<Region>> getRegions(@PathVariable Integer parentId) {
        return Result.success(regionService.getRegions(parentId));
    }
}