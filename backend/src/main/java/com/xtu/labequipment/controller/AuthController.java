package com.xtu.labequipment.controller;

import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.dto.LoginRequest;
import com.xtu.labequipment.dto.RegisterRequest;
import com.xtu.labequipment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginRequest request) { return Result.ok(authService.login(request)); }

    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid RegisterRequest request) { authService.register(request); return Result.ok(); }
}
