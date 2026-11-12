package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.Major;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MajorMapper {
    @Select("SELECT * FROM major ORDER BY sort ASC")
    List<Major> findAllMajors();
    @Insert("INSERT INTO major(c_name) VALUES(#{cName})")
    void insertMajor(Major major);

    @Delete("DELETE FROM major WHERE id = #{id}")
    void deleteMajorById(Integer id);
}