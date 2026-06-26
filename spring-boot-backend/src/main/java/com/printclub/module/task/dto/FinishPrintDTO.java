package com.printclub.module.task.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 完成打印请求
 *
 * <p>由技术骨干在打印结束后填写</p>
 *
 * @author E
 */
@Data
public class FinishPrintDTO {

    @NotNull(message = "实际耗材不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "实际耗材必须大于 0")
    private BigDecimal actualWeight;

    @NotNull(message = "实际耗时不能为空")
    @Min(value = 1, message = "实际耗时至少 1 分钟")
    private Integer actualTime;

    /** 质量评分 1-5（可选） */
    @Min(1) @Max(5)
    private Integer qualityScore;
}