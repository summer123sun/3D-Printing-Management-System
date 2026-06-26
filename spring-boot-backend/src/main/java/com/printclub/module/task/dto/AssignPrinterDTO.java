package com.printclub.module.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分配打印机请求
 *
 * @author E
 */
@Data
public class AssignPrinterDTO {

    @NotBlank(message = "打印机编号不能为空")
    private String printerId;
}