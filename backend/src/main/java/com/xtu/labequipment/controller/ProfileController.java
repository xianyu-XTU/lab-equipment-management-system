package com.xtu.labequipment.controller;

import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.BusinessException;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.dto.ChangePasswordRequest;
import com.xtu.labequipment.entity.User;
import com.xtu.labequipment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<User> currentUser() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        user.setPassword(request.getNewPassword());
        userService.updateById(user);
        return Result.ok();
    }
}
