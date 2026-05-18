package com.xtu.labequipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xtu.labequipment.common.BusinessException;
import com.xtu.labequipment.common.JwtUtil;
import com.xtu.labequipment.common.PasswordUtil;
import com.xtu.labequipment.dto.LoginRequest;
import com.xtu.labequipment.dto.LoginResponse;
import com.xtu.labequipment.dto.RegisterRequest;
import com.xtu.labequipment.entity.Role;
import com.xtu.labequipment.entity.User;
import com.xtu.labequipment.mapper.RoleMapper;
import com.xtu.labequipment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) throw new BusinessException("user not found");
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) throw new BusinessException("wrong password");
        if (user.getStatus() == null || user.getStatus() == 0) throw new BusinessException("user disabled");
        Role role = roleMapper.selectById(user.getRoleId());
        String roleCode = role == null ? "STUDENT" : role.getRoleCode();
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), roleCode);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), roleCode);
    }

    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) throw new BusinessException("username already exists");
        Role student = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "STUDENT"));
        if (student == null) throw new BusinessException("student role missing");
        User user = new User();
        user.setUsername(request.getUsername());
        validatePassword(request.getPassword());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRoleId(student.getId());
        user.setStatus(1);
        userMapper.insert(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 32) {
            throw new BusinessException("密码长度必须为6到32位");
        }
    }
}
