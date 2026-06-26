package com.printclub.module.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改技能等级请求
 *
 * @author A
 */
@Data
public class UpdateSkillDTO {

    @NotNull(message = "技能等级不能为空")
    @Min(value = 0, message = "技能等级非法")
    @Max(value = 4, message = "技能等级非法")
    private Integer skillLevel;
}
