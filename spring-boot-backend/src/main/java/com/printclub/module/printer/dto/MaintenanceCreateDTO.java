package com.printclub.module.printer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维护记录创建 DTO
 */
@Data
public class MaintenanceCreateDTO {

    @NotBlank
    private String printerId;

    @NotNull
    private Integer maintType;  // 1保养 2维修 3换件 4校准

    @NotBlank
    private String content;

    private LocalDateTime maintTime;  // 不传则取当前时间

    private LocalDate nextMaintDate;
}
