package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Policy;

import java.util.List;

public interface PolicyService {

    /** @param type 前端参数名，对应数据库 category_id（1 普通 2 免试） */
    List<Policy> getPolicies(Integer provinceId, Integer publisher, Integer type);

    List<Policy> getAllPolicies();

    /** 政策详情，顺便浏览量 +1；uid 用于判断当前用户是否已收藏 */
    Policy getPolicyDetail(Long id, Long uid);

    void addPolicy(Policy policy);

    void updatePolicy(Policy policy);

    void deletePolicy(Long id);
}
