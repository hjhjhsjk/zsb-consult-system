package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.Policy;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PolicyMapper {

    /**
     * 政策列表：province_id 必传，publisher / type 可选。
     * 用 <script> 写动态 SQL —— 这也是 MyBatis 注解方式里拼接条件的标准做法。
     * 注意：请求参数叫 type，数据库列叫 category_id，映射在 Controller 层完成。
     */
    @Select("<script>" +
            "SELECT * FROM policy WHERE province_id = #{provinceId} " +
            "<if test='publisher != null'> AND publisher = #{publisher} </if>" +
            "<if test='type != null'> AND category_id = #{type} </if>" +
            "ORDER BY is_top DESC, create_time DESC" +
            "</script>")
    List<Policy> findByProvince(@Param("provinceId") Integer provinceId,
                                @Param("publisher") Integer publisher,
                                @Param("type") Integer type);

    /**
     * 详情：顺带查当前用户是否收藏（uid=0 时恒为 0）。
     * 收藏按钮的初始状态就不用前端再单独请求一次收藏夹了。
     */
    @Select("SELECT p.*, (SELECT COUNT(*) FROM favorite f " +
            "WHERE f.target_type = 2 AND f.target_id = p.id AND f.user_id = #{uid}) AS is_fav " +
            "FROM policy p WHERE p.id = #{id}")
    Policy findById(@Param("id") Long id, @Param("uid") Long uid);

    @Select("SELECT * FROM policy ORDER BY is_top DESC, create_time DESC")
    List<Policy> findAllPolicies();

    @Insert("INSERT INTO policy(title, content, publisher, category_id, province_id, is_top, url) " +
            "VALUES(#{title}, #{content}, #{publisher}, #{categoryId}, #{provinceId}, " +
            "IFNULL(#{isTop}, 0), IFNULL(#{url}, ''))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPolicy(Policy policy);

    @Update("UPDATE policy SET title = #{title}, content = #{content}, publisher = #{publisher}, " +
            "category_id = #{categoryId}, province_id = #{provinceId}, is_top = IFNULL(#{isTop}, 0), " +
            "url = IFNULL(#{url}, '') WHERE id = #{id}")
    void updatePolicy(Policy policy);

    @Update("UPDATE policy SET view_count = view_count + 1 WHERE id = #{id}")
    void increaseViewCount(@Param("id") Long id);

    @Delete("DELETE FROM policy WHERE id = #{id}")
    void deletePolicyById(Long id);

    @Select("SELECT COUNT(*) FROM policy")
    int countAll();
}
