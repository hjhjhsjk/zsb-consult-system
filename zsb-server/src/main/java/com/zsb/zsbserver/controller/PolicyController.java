package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.entity.Policy;
import com.zsb.zsbserver.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/home")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    /**
     * D-01 政策列表
     * province_id 必传；publisher（1 政府 2 学校）和 type（1 普通 2 免试）可选。
     * 注意参数名叫 type，数据库列叫 category_id，所以在 Service/Mapper 层做转换。
     */
    @GetMapping("/getPolicies")
    public Result<List<Policy>> getPolicies(@RequestParam("province_id") Integer provinceId,
                                            @RequestParam(value = "publisher", required = false) Integer publisher,
                                            @RequestParam(value = "type", required = false) Integer type) {
        return Result.success(policyService.getPolicies(provinceId, publisher, type));
    }

    /** M-2 政策详情（前端刷新详情页不再依赖 sessionStorage）；uid 可选，用于带出收藏状态 */
    @GetMapping("/getPolicies/{id}")
    public Result<Policy> getPolicyDetail(@PathVariable Long id,
                                         @RequestParam(value = "uid", required = false) Long uid) {
        return Result.success(policyService.getPolicyDetail(id, uid));
    }
}
