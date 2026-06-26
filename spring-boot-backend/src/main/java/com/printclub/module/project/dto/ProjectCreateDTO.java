package com.printclub.module.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建项目请求（包含初始阶段列表）
 *
 * @author E
 */
@Data
public class ProjectCreateDTO {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100)
    private String projectName;

    @NotNull
    @Min(1) @Max(4)
    private Integer projectType;

    /** 负责人学号（不传则默认当前登录用户） */
    private String leaderId;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin("0")
    private BigDecimal budget = BigDecimal.ZERO;

    private String description;

    private String deliverables;

    /** 封面图 URL（前端先上传再传这里） */
    private String coverImage;

    /** 初始阶段列表（创建项目时可一并创建） */
    @Valid
    private List<StageDTO> stages;

    /** 初始成员列表（创建项目时可一并添加） */
    @Valid
    private List<AddMemberDTO> initialMembers;
}