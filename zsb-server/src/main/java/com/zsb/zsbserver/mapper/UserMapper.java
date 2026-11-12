package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.User;
import com.zsb.zsbserver.vo.UserInfoVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE uname = #{uname}")
    User findByUname(String uname);

    /** 登录鉴权用：拦截器要判断 ustate 是不是管理员 */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /** 个人中心：只查需要的列，pwd 永远不出库 */
    @Select("SELECT id AS uid, uname, nickname, avatar, province_id, city_id, major_id, year, ustate " +
            "FROM user WHERE id = #{uid}")
    UserInfoVO findInfoById(@Param("uid") Long uid);

    /** 后台查所有用户（不查密码） */
    @Select("SELECT id, uname, nickname, phone, avatar, major_id, year, region_id, province_id, city_id, ustate, create_time " +
            "FROM user ORDER BY id DESC")
    List<User> findAllUsers();

    @Delete("DELETE FROM user WHERE id = #{id}")
    void deleteUserById(Long id);

    /** 管理端调整角色：0=学生 3=管理员 */
    @Update("UPDATE user SET ustate = #{ustate} WHERE id = #{id}")
    void updateUstate(@Param("id") Long id, @Param("ustate") Integer ustate);

    @Insert("INSERT INTO user(uname, pwd, nickname, avatar, phone, major_id, year, region_id, province_id, city_id, ustate) " +
            "VALUES(#{uname}, #{pwd}, #{nickname}, #{avatar}, #{phone}, #{majorId}, #{year}, #{regionId}, #{provinceId}, #{cityId}, #{ustate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /** 个人信息修改：只允许改这些字段 */
    @Update("UPDATE user SET nickname = #{nickname}, avatar = #{avatar}, major_id = #{majorId}, " +
            "year = #{year}, region_id = #{regionId}, province_id = #{provinceId}, city_id = #{cityId} " +
            "WHERE id = #{id}")
    void updateInfo(User user);

    @Select("SELECT COUNT(*) FROM user")
    int countAll();

    /** 注册时判断手机号是否被占用（表上有 uk_phone 唯一索引，提前查能给出友好提示） */
    @Select("SELECT COUNT(*) FROM user WHERE phone = #{phone}")
    int countByPhone(@Param("phone") String phone);
}
