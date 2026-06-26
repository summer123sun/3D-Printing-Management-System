package com.printclub.module.user.dto;

import lombok.Data;

/**
 * 修改个人信息请求
 *
 * @author A
 */
@Data
public class UpdateUserInfoDTO {

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;
}
