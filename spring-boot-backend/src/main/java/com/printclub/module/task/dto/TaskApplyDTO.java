package com.printclub.module.task.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提交打印任务请求
 *
 * @author E
 */
@Data
public class TaskApplyDTO {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100)
    private String modelName;

    @NotBlank(message = "STL 文件路径不能为空")
    @Size(max = 255)
    private String stlFilePath;

    @NotBlank(message = "材料类型不能为空")
    @Pattern(regexp = "PLA|PETG|TPU|ABS", message = "材料类型必须是 PLA/PETG/TPU/ABS")
    private String materialType;

    @Size(max = 20)
    private String color;

    @NotNull(message = "层高不能为空")
    @DecimalMin(value = "0.05", message = "层高至少 0.05mm")
    @DecimalMax(value = "0.4", message = "层高最多 0.4mm")
    private BigDecimal layerHeight;

    @NotNull
    @Min(value = 0, message = "填充率至少 0")
    @Max(value = 100, message = "填充率最多 100")
    private Integer infillRate;

    @NotNull
    @Min(0) @Max(1)
    private Integer needSupport;

    @NotNull
    @Min(1) @Max(3)
    private Integer priority;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal estWeight;

    @Min(1)
    private Integer estTime;

    /** 可选：关联项目编号 */
    private Integer projectId;
}