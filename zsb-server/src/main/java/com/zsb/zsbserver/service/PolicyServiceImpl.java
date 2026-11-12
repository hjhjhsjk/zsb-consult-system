package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Policy;
import com.zsb.zsbserver.mapper.PolicyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyServiceImpl implements PolicyService {

    @Autowired
    private PolicyMapper policyMapper;

    @Override
    public List<Policy> getPolicies(Integer provinceId, Integer publisher, Integer type) {
        if (provinceId == null) {
            throw new IllegalArgumentException("缺少参数 province_id");
        }
        return policyMapper.findByProvince(provinceId, publisher, type);
    }

    @Override
    public List<Policy> getAllPolicies() {
        return policyMapper.findAllPolicies();
    }

    @Override
    public Policy getPolicyDetail(Long id, Long uid) {
        Policy policy = policyMapper.findById(id, uid == null ? 0L : uid);
        if (policy == null) {
            throw new IllegalArgumentException("政策不存在");
        }
        policyMapper.increaseViewCount(id);
        policy.setViewCount((policy.getViewCount() == null ? 0 : policy.getViewCount()) + 1);
        return policy;
    }

    @Override
    public void addPolicy(Policy policy) {
        validate(policy);
        policyMapper.insertPolicy(policy);
    }

    @Override
    public void updatePolicy(Policy policy) {
        if (policy.getId() == null) {
            throw new IllegalArgumentException("缺少政策 ID");
        }
        validate(policy);
        policyMapper.updatePolicy(policy);
    }

    @Override
    public void deletePolicy(Long id) {
        policyMapper.deletePolicyById(id);
    }

    private void validate(Policy policy) {
        if (policy.getTitle() == null || policy.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("政策标题不能为空");
        }
        if (policy.getContent() == null || policy.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("政策正文不能为空");
        }
        if (policy.getProvinceId() == null) {
            throw new IllegalArgumentException("请选择所属省份");
        }
        policy.setTitle(policy.getTitle().trim());
        policy.setPublisher(policy.getPublisher() == null ? 1 : policy.getPublisher());
        policy.setCategoryId(policy.getCategoryId() == null ? 1 : policy.getCategoryId());
    }
}
