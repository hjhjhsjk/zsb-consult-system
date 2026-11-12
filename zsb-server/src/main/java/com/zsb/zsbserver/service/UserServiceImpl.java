package com.zsb.zsbserver.service;

import com.zsb.zsbserver.common.JwtUtils;
import com.zsb.zsbserver.dto.LoginDTO;
import com.zsb.zsbserver.dto.RegisterDTO;
import com.zsb.zsbserver.dto.UserUpdateDTO;
import com.zsb.zsbserver.entity.Region;
import com.zsb.zsbserver.entity.User;
import com.zsb.zsbserver.mapper.RegionMapper;
import com.zsb.zsbserver.mapper.UserMapper;
import com.zsb.zsbserver.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RegionMapper regionMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /** 密码加密器：BCrypt 每次加密结果都不同，但 matches() 能校验 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(RegisterDTO dto) {
        // 1. 业务校验（数据库唯一索引是最后一道防线，这里提前查是为了给出人话提示）
        if (userMapper.findByUname(dto.getUname()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userMapper.countByPhone(dto.getPhone()) > 0) {
            throw new IllegalArgumentException("该手机号已注册");
        }

        // 2. 由「最细一级地区」推导省 / 市
        Region region = requireRegion(dto.getRegionId());

        // 3. DTO -> Entity
        User user = new User();
        user.setUname(dto.getUname());
        user.setPwd(passwordEncoder.encode(dto.getPwd()));
        user.setPhone(dto.getPhone());
        user.setMajorId(dto.getMajorId());
        user.setYear(dto.getYear() == null ? 0 : dto.getYear());
        user.setRegionId(region.getId());
        user.setProvinceId(provinceOf(region));
        // city_id 就是「用户选中的那个地区」：选到市就是市，选到直辖市时它等于省
        user.setCityId(region.getId());
        user.setNickname(isBlank(dto.getNickname()) ? dto.getUname() : dto.getNickname().trim());
        user.setAvatar(dto.getAvatar() == null ? "" : dto.getAvatar());
        user.setUstate(0); // 注册一律是学生，管理员只能在库里改

        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.findByUname(dto.getUname());
        if (user == null || !passwordEncoder.matches(dto.getPwd(), user.getPwd())) {
            // 不区分「用户不存在」和「密码错误」，避免被人撞库探测账号
            throw new IllegalArgumentException("用户名或密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtils.generateToken(user.getId()));
        result.put("uid", user.getId());
        // 多返回两个字段，方便管理端判断是不是管理员（ustate=3）
        result.put("ustate", user.getUstate());
        result.put("nickname", user.getNickname());
        return result;
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.findAllUsers();
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteUserById(id);
    }

    @Override
    public void updateUserState(Long targetUid, Integer targetState, Long operatorUid) {
        // 只允许两个合法落点：0=学生，3=管理员
        if (targetState == null || (targetState != 0 && targetState != 3)) {
            throw new IllegalArgumentException("非法的角色状态");
        }
        // 不能改自己的角色，防止管理员把自己降级后失去管理端入口
        if (targetUid.equals(operatorUid)) {
            throw new IllegalArgumentException("不能修改自己的角色");
        }
        if (userMapper.findById(targetUid) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.updateUstate(targetUid, targetState);
    }

    @Override
    public UserInfoVO getUserInfo(Long uid) {
        UserInfoVO vo = userMapper.findInfoById(uid);
        if (vo == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return vo;
    }

    @Override
    public void updateUser(Long uid, UserUpdateDTO dto) {
        User user = userMapper.findById(uid);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 只覆盖前端真正传了的字段（null 表示不改），避免前端漏传把已有数据清空
        if (dto.getNickname() != null) {
            String nickname = dto.getNickname().trim();
            if (nickname.isEmpty() || nickname.length() > 20) {
                throw new IllegalArgumentException("昵称长度需在 1~20 位之间");
            }
            user.setNickname(nickname);
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getMajorId() != null) {
            user.setMajorId(dto.getMajorId());
        }
        if (dto.getYear() != null) {
            user.setYear(dto.getYear());
        }
        if (dto.getRegionId() != null) {
            Region region = requireRegion(dto.getRegionId());
            user.setRegionId(region.getId());
            user.setProvinceId(provinceOf(region));
            user.setCityId(region.getId());
        }

        userMapper.updateInfo(user);
    }

    /** 地区必须存在，否则插库会触发外键错误，报出来的信息也看不懂 */
    private Region requireRegion(Integer regionId) {
        if (regionId == null) {
            throw new IllegalArgumentException("请选择所在地区");
        }
        Region region = regionMapper.findById(regionId);
        if (region == null) {
            throw new IllegalArgumentException("地区不存在");
        }
        return region;
    }

    /** 省 ID：选的是省（parent_id=0）就是自己，选的是市就是它的父级 */
    private Integer provinceOf(Region region) {
        return region.getParentId() == 0 ? region.getId() : region.getParentId();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
