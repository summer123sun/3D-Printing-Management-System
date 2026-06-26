package com.printclub.module.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改角色请求
 *
 * @author A
 */
@Data
public class UpdateRoleDTO {

    @NotNull(message = "角色不能为空")
    @Min(value = 1, message = "角色值非法")
    @Max(value = 4, message = "角色值非法")
    private Integer role;
}
