package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.RecruitmentPlan;
import com.zsb.zsbserver.vo.SearchPlanVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecruitmentPlanMapper {

    @Select("SELECT * FROM recruitment_plan WHERE school_id = #{schoolId} ORDER BY year DESC, id ASC")
    List<RecruitmentPlan> findBySchoolId(@Param("schoolId") Integer schoolId);

    @Select("SELECT * FROM recruitment_plan ORDER BY year DESC, id ASC")
    List<RecruitmentPlan> findAll();

    @Select("SELECT * FROM recruitment_plan WHERE id = #{id}")
    RecruitmentPlan findById(@Param("id") Long id);

    @Insert("INSERT INTO recruitment_plan(school_id, school_name, school_logo, major_id, major_name, " +
            "year, plan_count, tuition_fee, recruitment_type) VALUES(#{schoolId}, #{schoolName}, #{schoolLogo}, " +
            "#{majorId}, #{majorName}, #{year}, #{planCount}, #{tuitionFee}, #{recruitmentType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RecruitmentPlan plan);

    @Update("UPDATE recruitment_plan SET school_id = #{schoolId}, school_name = #{schoolName}, " +
            "school_logo = #{schoolLogo}, major_id = #{majorId}, major_name = #{majorName}, year = #{year}, " +
            "plan_count = #{planCount}, tuition_fee = #{tuitionFee}, recruitment_type = #{recruitmentType} " +
            "WHERE id = #{id}")
    void update(RecruitmentPlan plan);

    @Delete("DELETE FROM recruitment_plan WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    /**
     * 全局搜索：院校名或专业名命中关键词。
     * 返回的 id 是「院校 ID」（前端拿它跳 /school/{id}），
     * school_type 直接在 SQL 里 CASE 成中文，省得前端再写映射。
     */
    @Select("<script>" +
            "SELECT s.id, s.school_name, s.logo_url AS school_logo, rp.major_name, " +
            "       rp.tuition_fee, rp.plan_count, rp.year, " +
            "       CASE s.school_type WHEN 1 THEN '公办' ELSE '民办' END AS school_type " +
            "FROM recruitment_plan rp JOIN school s ON s.id = rp.school_id " +
            "WHERE (rp.major_name LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR s.school_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "<if test='regionId != null'> AND s.province_id = #{regionId} </if>" +
            "ORDER BY s.id ASC, rp.year DESC" +
            "</script>")
    List<SearchPlanVO> search(@Param("keyword") String keyword, @Param("regionId") Integer regionId);

    @Select("SELECT COUNT(*) FROM recruitment_plan")
    int countAll();
}
