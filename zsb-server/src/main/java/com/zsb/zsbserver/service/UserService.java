package com.zsb.zsbserver.service;

import com.zsb.zsbserver.dto.LoginDTO;
import com.zsb.zsbserver.dto.RegisterDTO;
import com.zsb.zsbserver.dto.UserUpdateDTO;
import com.zsb.zsbserver.entity.User;
import com.zsb.zsbserver.vo.UserInfoVO;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<User> getAllUsers();

    void deleteUser(Long id);

    /** 管理端调整角色：target 只允许 0（学生）或 3（管理员），不能改自己 */
    void updateUserState(Long targetUid, Integer targetState, Long operatorUid);

    void register(RegisterDTO registerDTO);

    Map<String, Object> login(LoginDTO loginDTO);

    /** 个人中心资料 */
    UserInfoVO getUserInfo(Long uid);

    /** 修改昵称/头像/专业/年份/地区 */
    void updateUser(Long uid, UserUpdateDTO dto);
}
