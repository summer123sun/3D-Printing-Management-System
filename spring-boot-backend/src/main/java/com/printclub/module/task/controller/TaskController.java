package com.printclub.module.task.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.Result;
import com.printclub.module.task.dto.*;
import com.printclub.module.task.entity.PrintTask;
import com.printclub.module.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务 Controller（E - 核心业务）
 *
 * @author E
 */
@Tag(name = "打印任务")
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "提交打印任务")
    @PostMapping
    @RequireAuth
    public Result<String> apply(@RequestBody @Valid TaskApplyDTO dto) {
        return Result.success(taskService.apply(dto));
    }

    @Operation(summary = "我的任务列表")
    @GetMapping("/my")
    @RequireAuth
    public Result<PageResult<PrintTask>> myTasks(TaskQuery query) {
        return Result.success(taskService.myTasks(query));
    }

    @Operation(summary = "待审批任务列表（技术骨干）")
    @GetMapping("/pending")
    @RequireRole({1, 2})
    public Result<PageResult<PrintTask>> pendingTasks(TaskQuery query) {
        return Result.success(taskService.pendingTasks(query));
    }

    @Operation(summary = "打印队列")
    @GetMapping("/queue")
    @RequireAuth
    public Result<PageResult<PrintTask>> queue(TaskQuery query) {
        return Result.success(taskService.queue(query));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    @RequireAuth
    public Result<PrintTask> detail(@PathVariable("id") String id) {
        return Result.success(taskService.detail(id));
    }

    @Operation(summary = "审批通过（技术骨干）")
    @PutMapping("/{id}/approve")
    @RequireRole({1, 2})
    public Result<Void> approve(@PathVariable("id") String id, @RequestBody(required = false) ApproveDTO dto) {
        if (dto == null) dto = new ApproveDTO();
        taskService.approve(id, dto);
        return Result.success();
    }

    @Operation(summary = "审批驳回（技术骨干）")
    @PutMapping("/{id}/reject")
    @RequireRole({1, 2})
    public Result<Void> reject(@PathVariable("id") String id, @RequestBody @Valid RejectDTO dto) {
        taskService.reject(id, dto);
        return Result.success();
    }

    @Operation(summary = "分配打印机（技术骨干）")
    @PutMapping("/{id}/assign")
    @RequireRole({1, 2})
    public Result<Void> assign(@PathVariable("id") String id, @RequestBody @Valid AssignPrinterDTO dto) {
        taskService.assignPrinter(id, dto);
        return Result.success();
    }

    @Operation(summary = "开始打印（技术骨干）")
    @PutMapping("/{id}/start")
    @RequireRole({1, 2})
    public Result<Void> start(@PathVariable("id") String id) {
        taskService.startPrint(id);
        return Result.success();
    }

    @Operation(summary = "标记完成（技术骨干）")
    @PutMapping("/{id}/finish")
    @RequireRole({1, 2})
    public Result<Void> finish(@PathVariable("id") String id, @RequestBody @Valid FinishPrintDTO dto) {
        taskService.finishPrint(id, dto);
        return Result.success();
    }

    @Operation(summary = "取件签到（申请人）")
    @PutMapping("/{id}/pickup")
    @RequireAuth
    public Result<Void> pickup(@PathVariable("id") String id, @RequestBody(required = false) PickupDTO dto) {
        if (dto == null) dto = new PickupDTO();
        taskService.pickup(id, dto);
        return Result.success();
    }

    @Operation(summary = "取消任务（申请人）")
    @PutMapping("/{id}/cancel")
    @RequireAuth
    public Result<Void> cancel(@PathVariable("id") String id) {
        taskService.cancel(id);
        return Result.success();
    }

    /**
     * 修改任务（仅 priority / title 等可改字段；任务流转请用专门接口）
     * 技术骨干及以上可改优先级
     */
    @Operation(summary = "修改任务（priority / title）")
    @PutMapping("/{id}")
    @RequireRole({1, 2})
    public Result<Void> update(@PathVariable("id") String id, @RequestBody(required = false) @Valid UpdateTaskDTO dto) {
        // ✅ v2.2 防御性：dto 可能 null（前端没传 body），给个空对象让 service 走"不修改"分支
        if (dto == null) {
            dto = new UpdateTaskDTO();
        }
        taskService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "任务统计（看板）")
    @GetMapping("/stats")
    @RequireRole({1, 2})
    public Result<Object> stats() {
        return Result.success(taskService.stats());
    }
}