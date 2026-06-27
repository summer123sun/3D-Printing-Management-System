package com.printclub.module.log.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.Result;
import com.printclub.module.log.dto.LogQuery;
import com.printclub.module.log.entity.SystemLog;
import com.printclub.module.log.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统日志 Controller（M6 - 审计：仅 ADMIN 可读）
 *
 * @author F
 */
@Tag(name = "系统日志")
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @Operation(summary = "日志列表（ADMIN）")
    @GetMapping
    @RequireAuth
    @RequireRole({1})
    public Result<PageResult<SystemLog>> list(LogQuery query) {
        return Result.success(logService.list(query));
    }

    @Operation(summary = "清理过期日志（ADMIN，默认保留 90 天）")
    @DeleteMapping("/clean")
    @RequireAuth
    @RequireRole({1})
    public Result<Integer> clean(@RequestParam(defaultValue = "90") int keepDays) {
        return Result.success(logService.cleanOldLogs(keepDays));
    }
}
