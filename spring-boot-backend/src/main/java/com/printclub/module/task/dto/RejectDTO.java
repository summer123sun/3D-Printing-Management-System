package com.printclub.module.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 驳回请求
 *
 * @author E
 */
@Data
public class RejectDTO {

    @NotBlank(message = "驳回原因必填")
    private String approveComment;
}