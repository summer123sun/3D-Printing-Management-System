package com.printclub.module.project.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.Result;
import com.printclub.module.project.dto.*;
import com.printclub.module.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 项目 Controller（E - 核心业务）
 *
 * @author E
 */
@Tag(name = "项目管理")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ============== 项目 CRUD ==============

    @Operation(summary = "创建项目")
    @PostMapping
    @RequireRole({1, 2})
    public Result<Integer> create(@RequestBody @Valid ProjectCreateDTO dto) {
        return Result.success(projectService.create(dto));
    }

    @Operation(summary = "项目列表")
    @GetMapping("/list")
    @RequireAuth
    public Result<PageResult<?>> list(ProjectQuery query) {
        return Result.success(projectService.list(query));
    }

    @Operation(summary = "项目详情（聚合 4 张表）")
    @GetMapping("/{id}")
    @RequireAuth
    public Result<ProjectDetailVO> detail(@PathVariable("id") Integer id) {
        return Result.success(projectService.detail(id));
    }

    @Operation(summary = "修改项目（项目负责人）")
    @PutMapping("/{id}")
    @RequireAuth
    public Result<Void> update(@PathVariable("id") Integer id, @RequestBody @Valid ProjectCreateDTO dto) {
        projectService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "标记完成（项目负责人）")
    @PutMapping("/{id}/complete")
    @RequireAuth
    public Result<Void> complete(@PathVariable("id") Integer id) {
        projectService.complete(id);
        return Result.success();
    }

    @Operation(summary = "取消项目（项目负责人）")
    @PutMapping("/{id}/cancel")
    @RequireAuth
    public Result<Void> cancel(@PathVariable("id") Integer id) {
        projectService.cancel(id);
        return Result.success();
    }

    // ============== 成员管理 ==============

    @Operation(summary = "添加成员（项目负责人）")
    @PostMapping("/{id}/member")
    @RequireAuth
    public Result<Void> addMember(@PathVariable("id") Integer id, @RequestBody @Valid AddMemberDTO dto) {
        projectService.addMember(id, dto);
        return Result.success();
    }

    @Operation(summary = "移除成员（项目负责人）")
    @DeleteMapping("/{id}/member/{mid}")
    @RequireAuth
    public Result<Void> removeMember(@PathVariable("id") Integer id, @PathVariable("mid") String mid) {
        projectService.removeMember(id, mid);
        return Result.success();
    }

    @Operation(summary = "修改成员角色（项目负责人）")
    @PutMapping("/{id}/member/{mid}")
    @RequireAuth
    public Result<Void> updateMemberRole(@PathVariable("id") Integer id,
                                          @PathVariable("mid") String mid,
                                          @RequestBody AddMemberDTO dto) {
        projectService.updateMemberRole(id, mid, dto.getRoleInProject());
        return Result.success();
    }

    // ============== 阶段管理 ==============

    @Operation(summary = "添加阶段（项目负责人）")
    @PostMapping("/{id}/progress")
    @RequireAuth
    public Result<Integer> addStage(@PathVariable("id") Integer id, @RequestBody @Valid StageDTO dto) {
        return Result.success(projectService.addStage(id, dto));
    }

    @Operation(summary = "修改阶段（项目负责人）")
    @PutMapping("/{id}/progress/{pid}")
    @RequireAuth
    public Result<Void> updateStage(@PathVariable("id") Integer id,
                                     @PathVariable("pid") Integer pid,
                                     @RequestBody @Valid StageDTO dto) {
        projectService.updateStage(id, pid, dto);
        return Result.success();
    }

    @Operation(summary = "更新阶段状态（阶段负责人）")
    @PutMapping("/{id}/progress/{pid}/status")
    @RequireAuth
    public Result<Void> updateStageStatus(@PathVariable("id") Integer id,
                                            @PathVariable("pid") Integer pid,
                                            @RequestParam Integer status) {
        projectService.updateStageStatus(id, pid, status);
        return Result.success();
    }

    @Operation(summary = "删除阶段（项目负责人）")
    @DeleteMapping("/{id}/progress/{pid}")
    @RequireAuth
    public Result<Void> deleteStage(@PathVariable("id") Integer id, @PathVariable("pid") Integer pid) {
        projectService.deleteStage(id, pid);
        return Result.success();
    }
}