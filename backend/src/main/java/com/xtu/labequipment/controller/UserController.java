package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.annotation.RequireRole;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@RequireRole({"ADMIN"})
public class UserController {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @GetMapping
    public Result<?> page(@RequestParam(defaultValue = "1") long page,
                          @RequestParam(defaultValue = "10") long size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) Long roleId) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(User::getUsername, keyword).or().like(User::getRealName, keyword).or().like(User::getPhone, keyword));
        }
        if (status != null) w.eq(User::getStatus, status);
        if (roleId != null) w.eq(User::getRoleId, roleId);
        w.orderByDesc(User::getId);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), w);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return Result.ok(user);
    }

    @PostMapping
    public Result<?> save(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) throw new BusinessException("用户名不能为空");
        if (user.getPassword() == null || user.getPassword().isBlank()) user.setPassword("123456");
        validatePassword(user.getPassword());
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        if (user.getRoleId() == null) throw new BusinessException("角色不能为空");
        if (roleMapper.selectById(user.getRoleId()) == null) throw new BusinessException("角色不存在");
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (exists != null && exists > 0) throw new BusinessException("用户名已存在");
        if (user.getStatus() == null) user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return Result.ok();
    }

    @PutMapping
    public Result<?> update(@RequestBody User user) {
        if (user.getId() == null) throw new BusinessException("用户ID不能为空");
        User old = userMapper.selectById(user.getId());
        if (old == null) throw new BusinessException("用户不存在");
        user.setUsername(null);
        if (user.getPassword() != null) {
            if (user.getPassword().isBlank()) {
                user.setPassword(null);
            } else {
                validatePassword(user.getPassword());
                user.setPassword(PasswordUtil.encode(user.getPassword()));
            }
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status != 0 && status != 1) throw new BusinessException("状态只能是0或1");
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok();
    }

    @GetMapping("/roles")
    public Result<?> roles() {
        return Result.ok(roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByAsc(Role::getId)));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 32) {
            throw new BusinessException("密码长度必须为6到32位");
        }
    }
}
