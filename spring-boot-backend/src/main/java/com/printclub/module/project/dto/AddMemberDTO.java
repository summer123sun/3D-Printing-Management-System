package com.printclub.module.project.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 添加项目成员请求
 *
 * @author E
 */
@Data
public class AddMemberDTO {

    @NotBlank(message = "成员学号不能为空")
    private String memberId;

    @NotNull
    @Min(1) @Max(3)
    private Integer roleInProject;

    @Size(max = 255)
    private String contribution;
}