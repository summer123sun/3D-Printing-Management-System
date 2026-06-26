package com.printclub.module.auth.dto;

import com.printclub.module.user.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录返回
 *
 * @author D
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** JWT token（前端存 localStorage） */
    private String token;

    /** 当前登录用户信息 */
    private Member user;
}