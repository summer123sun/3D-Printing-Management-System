package com.printclub.module.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 新增成员请求（仅社长可调）
 * <p>
 * 必填：studentId / name / role
 * 默认值：skillLevel=0（未入门）、password=123456、joinDate=今天
 *
 * @author A
 */
@Data
public class AddMemberDTO {

    /** 学号（10 位数字） */
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{10}$", message = "学号必须是 10 位数字")
    private String studentId;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 20, message = "姓名长度不能超过 20")
    private String name;

    /** 角色：1 社长 2 技术骨干 3 普通社员 4 新成员 */
    @NotNull(message = "角色不能为空")
    @Min(value = 1, message = "角色值非法")
    @Max(value = 4, message = "角色值非法")
    private Integer role;

    /** 技能等级（可选，默认 0 未入门） */
    @Min(value = 0, message = "技能等级非法")
    @Max(value = 4, message = "技能等级非法")
    private Integer skillLevel;

    /** 初始密码（可选，默认 123456） */
    @Size(min = 6, max = 30, message = "密码长度 6-30 位")
    private String password;

    /** 手机号（可选） */
    @Size(max = 20, message = "手机号长度过长")
    private String phone;

    /** 邮箱（可选） */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度过长")
    private String email;

    /** 入社日期（可选，默认今天） */
    private LocalDate joinDate;
}
