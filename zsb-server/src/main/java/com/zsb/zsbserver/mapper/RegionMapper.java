package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegionMapper {

    @Select("SELECT * FROM region WHERE parent_id = 0 ORDER BY sort ASC")
    List<Region> findProvinces();

    @Select("SELECT * FROM region WHERE parent_id = #{parentId} ORDER BY sort ASC")
    List<Region> findCitiesByProvinceId(@Param("parentId") Integer parentId);

    /** 注册/改资料时用：拿一个地区，判断它是省（parent_id=0）还是市 */
    @Select("SELECT * FROM region WHERE id = #{id}")
    Region findById(@Param("id") Integer id);
}
