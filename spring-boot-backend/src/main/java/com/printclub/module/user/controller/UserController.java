package com.printclub.module.user.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.Result;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.user.dto.*;
import com.printclub.module.user.entity.Member;
import com.printclub.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户 Controller
 *
 * @author A
 */
@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "成员列表（分页 + 关键字搜索 + 角色筛选）")
    @GetMapping("/list")
    @RequireAuth
    public Result<PageResult<Member>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role
    ) {
        return Result.success(userService.list(page, size, keyword, role));
    }

    @Operation(summary = "修改成员角色（仅社长）")
    @PutMapping("/{studentId}/role")
    @RequireRole({1})
    public Result<Void> updateRole(
            @PathVariable String studentId,
            @RequestBody @Valid UpdateRoleDTO dto
    ) {
        userService.updateRole(studentId, dto);
        return Result.success();
    }

    @Operation(summary = "修改成员技能等级")
    @PutMapping("/{studentId}/skill")
    @RequireRole({1, 2})
    public Result<Void> updateSkill(
            @PathVariable String studentId,
            @RequestBody @Valid UpdateSkillDTO dto
    ) {
        userService.updateSkill(studentId, dto);
        return Result.success();
    }

    @Operation(summary = "个人统计")
    @GetMapping("/{studentId}/stats")
    @RequireAuth
    public Result<UserStatsVO> stats(@PathVariable String studentId) {
        return Result.success(userService.stats(studentId));
    }

    @Operation(summary = "获取当前登录用户信息（前端路由守卫用）")
    @GetMapping("/info")
    @RequireAuth
    public Result<Member> currentInfo() {
        String studentId = SecurityContext.getCurrentUserId();
        if (studentId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(userService.getByStudentId(studentId));
    }

    @Operation(summary = "修改个人信息")
    @PostMapping("/info")
    @RequireAuth
    public Result<Void> updateInfo(@RequestBody UpdateUserInfoDTO dto) {
        userService.updateInfo(dto);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password")
    @RequireAuth
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        userService.changePassword(dto);
        return Result.success();
    }
}
