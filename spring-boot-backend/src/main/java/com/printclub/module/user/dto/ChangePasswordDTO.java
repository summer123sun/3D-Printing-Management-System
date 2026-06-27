package com.printclub.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求
 *
 * @author A
 */
@Data
public class ChangePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度 6-20 位")
    private String newPassword;

    /** 自定义校验：新旧密码不能相同（@AssertTrue 方法） */
    @jakarta.validation.constraints.AssertTrue(message = "新密码不能与旧密码相同")
    public boolean isNewPasswordDifferent() {
        if (oldPassword == null || newPassword == null) return true;  // 交给 @NotBlank 处理
        return !oldPassword.equals(newPassword);
    }
}
