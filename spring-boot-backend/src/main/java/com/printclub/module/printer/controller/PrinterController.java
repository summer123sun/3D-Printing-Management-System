package com.printclub.module.printer.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.Result;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.printer.dto.MaintenanceCreateDTO;
import com.printclub.module.printer.entity.Maintenance;
import com.printclub.module.printer.entity.Printer;
import com.printclub.module.printer.service.PrinterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打印机 Controller（M5 - 管理端：STAFF+ 维护，ADMIN 增删改）
 *
 * @author F
 */
@Tag(name = "打印机管理")
@RestController
@RequestMapping("/api/printer")
@RequiredArgsConstructor
public class PrinterController {

    private final PrinterService printerService;

    @Operation(summary = "打印机列表")
    @GetMapping
    @RequireAuth
    public Result<PageResult<Printer>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(printerService.list(page, size, status, keyword));
    }

    @Operation(summary = "可用打印机列表")
    @GetMapping("/available")
    @RequireAuth
    public Result<List<Printer>> available() {
        return Result.success(printerService.availablePrinters());
    }

    @Operation(summary = "打印机详情")
    @GetMapping("/{id}")
    @RequireAuth
    public Result<Printer> detail(@PathVariable("id") String id) {
        return Result.success(printerService.detail(id));
    }

    @Operation(summary = "新增打印机（ADMIN）")
    @PostMapping
    @RequireAuth
    @RequireRole({1})
    public Result<Void> create(@RequestBody @Valid Printer printer) {
        printerService.create(printer);
        return Result.success();
    }

    @Operation(summary = "修改打印机（ADMIN）")
    @PutMapping("/{id}")
    @RequireAuth
    @RequireRole({1})
    public Result<Void> update(@PathVariable("id") String id, @RequestBody @Valid Printer printer) {
        printer.setPrinterId(id);
        printerService.update(printer);
        return Result.success();
    }

    @Operation(summary = "删除打印机（ADMIN，限报废）")
    @DeleteMapping("/{id}")
    @RequireAuth
    @RequireRole({1})
    public Result<Void> delete(@PathVariable("id") String id) {
        printerService.delete(id);
        return Result.success();
    }

    @Operation(summary = "设置打印机状态（STAFF+）")
    @PutMapping("/{id}/status")
    @RequireAuth
    @RequireRole({1, 2})
    public Result<Void> setStatus(
            @PathVariable("id") String id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark
    ) {
        printerService.setStatus(id, status, remark);
        return Result.success();
    }

    // ============ 维护记录 ============

    @Operation(summary = "维护记录列表")
    @GetMapping("/maintenance")
    @RequireAuth
    public Result<PageResult<Maintenance>> maintenanceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String printerId
    ) {
        return Result.success(printerService.maintenanceList(page, size, printerId));
    }

    @Operation(summary = "新增维护记录（STAFF+）")
    @PostMapping("/maintenance")
    @RequireAuth
    @RequireRole({1, 2})
    public Result<Void> addMaintenance(@RequestBody @Valid MaintenanceCreateDTO dto) {
        printerService.addMaintenance(dto, SecurityContext.getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "删除维护记录（ADMIN）")
    @DeleteMapping("/maintenance/{id}")
    @RequireAuth
    @RequireRole({1})
    public Result<Void> deleteMaintenance(@PathVariable("id") Integer id) {
        printerService.deleteMaintenance(id);
        return Result.success();
    }
}
