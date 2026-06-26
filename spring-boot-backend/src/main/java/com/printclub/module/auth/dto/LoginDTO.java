package com.printclub.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求
 *
 * @author D
 */
@Data
public class LoginDTO {

    @NotBlank(message = "学号不能为空")
    @Size(min = 8, max = 20, message = "学号长度 8-20 位")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String password;
}