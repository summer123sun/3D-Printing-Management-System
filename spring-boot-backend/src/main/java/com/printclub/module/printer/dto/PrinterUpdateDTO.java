package com.printclub.module.printer.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 打印机更新 DTO（v2.13 修复：之前用 @RequestBody Printer entity 接参数）
 *
 * 问题：前端表单只填 8 个字段（printerId / model / brand / purchaseDate / location /
 *      nozzleSize / buildVolume / remark），其他字段（status / totalPrintHours / createTime）
 *      全是 undefined，Jackson 反序列化时填 null，MP updateById 会把这些字段改成 null
 *      → 社长编辑打印机后 status 变 NULL，totalPrintHours 归零
 *
 * 修法：只接收前端要改的字段，service 层手动 merge 到 entity，未传入字段保留原值
 */
@Data
public class PrinterUpdateDTO {

    @Size(max = 50, message = "型号长度不能超过 50")
    private String model;

    @Size(max = 50, message = "品牌长度不能超过 50")
    private String brand;

    private BigDecimal nozzleSize;

    @Size(max = 50, message = "构建尺寸长度不能超过 50")
    private String buildVolume;

    @Size(max = 100, message = "位置长度不能超过 100")
    private String location;

    private LocalDate purchaseDate;

    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;
}
