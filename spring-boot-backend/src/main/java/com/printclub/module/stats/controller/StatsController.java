package com.printclub.module.stats.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.result.Result;
import com.printclub.module.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计 Controller（M6 - 大屏 + 看板）
 *
 * @author F
 */
@Tag(name = "数据统计")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "总览 Dashboard")
    @GetMapping("/dashboard")
    @RequireAuth
    public Result<Object> dashboard() {
        return Result.success(statsService.dashboard());
    }

    @Operation(summary = "任务统计（按月/状态/材料）")
    @GetMapping("/task")
    @RequireAuth
    public Result<Object> taskStats(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        return Result.success(statsService.taskStats(startDate, endDate));
    }

    @Operation(summary = "材料消耗统计（按材料/月份）")
    @GetMapping("/material")
    @RequireAuth
    public Result<Object> materialStats() {
        return Result.success(statsService.materialStats());
    }

    @Operation(summary = "打印机使用率")
    @GetMapping("/printer")
    @RequireAuth
    public Result<Object> printerStats() {
        return Result.success(statsService.printerStats());
    }

    @Operation(summary = "活跃社员排行")
    @GetMapping("/member")
    @RequireAuth
    public Result<Object> memberRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.memberRanking(limit));
    }
}
