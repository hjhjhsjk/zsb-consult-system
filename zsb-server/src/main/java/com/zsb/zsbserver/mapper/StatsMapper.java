package com.zsb.zsbserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 管理端仪表盘统计：一张 SQL 出 5 个数字，避免前端串行发 5 个请求。
 * MyBatis 返回 Map 时，key 就是 SQL 里的别名。
 */
@Mapper
public interface StatsMapper {

    @Select("SELECT " +
            " (SELECT COUNT(*) FROM user) AS userCount, " +
            " (SELECT COUNT(*) FROM post WHERE status = 1) AS postCount, " +
            " (SELECT COUNT(*) FROM policy) AS policyCount, " +
            " (SELECT COUNT(*) FROM school) AS schoolCount, " +
            " (SELECT COUNT(*) FROM recruitment_plan) AS planCount")
    Map<String, Object> stats();
}
