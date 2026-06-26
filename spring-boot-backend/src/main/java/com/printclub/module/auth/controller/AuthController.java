package com.printclub.module.auth.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.result.Result;
import com.printclub.module.auth.dto.LoginDTO;
import com.printclub.module.auth.dto.LoginVO;
import com.printclub.module.auth.service.AuthService;
import com.printclub.module.user.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller
 *
 * @author D
 */
@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 前后端分离：前端清 token 即可，后端无需操作（JWT 无状态）
        // 如需黑名单，加 Redis 即可
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    @RequireAuth
    public Result<Member> info() {
        return Result.success(authService.getCurrentUser());
    }
}