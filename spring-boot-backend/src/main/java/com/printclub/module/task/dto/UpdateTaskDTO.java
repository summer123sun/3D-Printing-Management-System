package com.printclub.module.task.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 修改任务请求（部分字段可选）
 *
 * @author Mavis
 */
@Data
public class UpdateTaskDTO {

    /**
     * 优先级 1=紧急 2=普通 3=低优
     */
    @Min(value = 1, message = "优先级必须在 1-3 之间")
    @Max(value = 3, message = "优先级必须在 1-3 之间")
    private Integer priority;
}