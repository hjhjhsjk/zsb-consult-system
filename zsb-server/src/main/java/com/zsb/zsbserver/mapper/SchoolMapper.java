package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.School;
import com.zsb.zsbserver.vo.SchoolDetailVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SchoolMapper {

    @Select("SELECT * FROM school WHERE province_id = #{provinceId} AND school_type = #{type} ORDER BY id ASC")
    List<School> findByProvinceAndType(@Param("provinceId") Integer provinceId, @Param("type") Integer type);

    /** 院校详情：logo_url 与 school_logo 双字段同返，兼容前端两种写法 */
    @Select("SELECT id, school_name, logo_url, logo_url AS school_logo, city_name, school_type, province_id " +
            "FROM school WHERE id = #{id}")
    SchoolDetailVO findDetailById(@Param("id") Integer id);

    @Select("SELECT * FROM school WHERE id = #{id}")
    School findById(@Param("id") Integer id);

    @Insert("INSERT INTO school(school_name, logo_url, city_name, school_type, province_id, city_id) " +
            "VALUES(#{schoolName}, #{logoUrl}, #{cityName}, #{schoolType}, #{provinceId}, #{cityId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSchool(School school);

    @Update("UPDATE school SET school_name = #{schoolName}, logo_url = #{logoUrl}, city_name = #{cityName}, " +
            "school_type = #{schoolType}, province_id = #{provinceId}, city_id = #{cityId} WHERE id = #{id}")
    void updateSchool(School school);

    @Delete("DELETE FROM school WHERE id = #{id}")
    void deleteSchoolById(Integer id);

    @Select("SELECT COUNT(*) FROM school")
    int countAll();
}
