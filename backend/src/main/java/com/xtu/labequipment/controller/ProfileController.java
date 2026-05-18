package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.BusinessException;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.common.PasswordUtil;
import com.xtu.labequipment.entity.Role;
import com.xtu.labequipment.entity.User;
import com.xtu.labequipment.mapper.RoleMapper;
import com.xtu.labequipment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @GetMapping
    public Result<?> current() {
        User user = userMapper.selectById(AuthContext.getUserId());
        if (user == null) throw new BusinessException("用户不存在");
        Role role = roleMapper.selectById(user.getRoleId());
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("role", role);
        return Result.ok(data);
    }

    @PutMapping
    public Result<?> update(@RequestBody User input) {
        User user = userMapper.selectById(AuthContext.getUserId());
        if (user == null) throw new BusinessException("用户不存在");
        user.setRealName(input.getRealName());
        user.setPhone(input.getPhone());
        user.setEmail(input.getEmail());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 32) throw new BusinessException("新密码长度必须为6到32位");
        User user = userMapper.selectById(AuthContext.getUserId());
        if (user == null) throw new BusinessException("用户不存在");
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) throw new BusinessException("原密码错误");
        user.setPassword(PasswordUtil.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok();
    }
}
