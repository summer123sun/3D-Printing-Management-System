package com.printclub.module.project.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 阶段 DTO
 *
 * <p>用于创建项目时嵌套传递阶段列表</p>
 *
 * @author E
 */
@Data
public class StageDTO {

    @NotBlank(message = "阶段名称不能为空")
    @Size(max = 50)
    private String stageName;

    @NotNull
    @Min(1) @Max(99)
    private Integer stageOrder;

    private String description;

    /** 阶段负责人学号（可选） */
    private String responsibleId;
}