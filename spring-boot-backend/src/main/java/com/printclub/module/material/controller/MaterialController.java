package com.printclub.module.material.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.Result;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.material.dto.MaterialInboundDTO;
import com.printclub.module.material.dto.MaterialStockVO;
import com.printclub.module.material.entity.MaterialLog;
import com.printclub.module.material.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 耗材 Controller（M5 - 管理端：所有人能看，STAFF+ 能入库）
 *
 * @author F
 */
@Tag(name = "耗材管理")
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "库存列表")
    @GetMapping
    @RequireAuth
    public Result<List<MaterialStockVO>> list(@RequestParam(required = false) String materialType) {
        return Result.success(materialService.stockList(materialType));
    }

    @Operation(summary = "库存预警")
    @GetMapping("/warning")
    @RequireAuth
    public Result<List<MaterialStockVO>> warning(@RequestParam(required = false) BigDecimal threshold) {
        return Result.success(materialService.warningStocks(threshold));
    }

    @Operation(summary = "耗材流水")
    @GetMapping("/log")
    @RequireAuth
    public Result<PageResult<MaterialLog>> logList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String materialType,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer operationType,
            @RequestParam(required = false) String operatorId
    ) {
        return Result.success(materialService.logList(page, size, materialType, color, operationType, operatorId));
    }

    @Operation(summary = "入库（STAFF+）")
    @PostMapping("/inbound")
    @RequireAuth
    @RequireRole({1, 2})
    public Result<Void> inbound(@RequestBody @Valid MaterialInboundDTO dto) {
        materialService.inbound(dto, SecurityContext.getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "总览统计")
    @GetMapping("/summary")
    @RequireAuth
    public Result<Object> summary() {
        return Result.success(materialService.summary());
    }
}
