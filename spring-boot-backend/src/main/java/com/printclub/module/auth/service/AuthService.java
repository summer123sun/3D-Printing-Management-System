package com.printclub.module.auth.service;

import com.printclub.module.auth.dto.LoginDTO;
import com.printclub.module.auth.dto.LoginVO;
import com.printclub.module.user.entity.Member;

/**
 * 认证 Service
 *
 * @author D
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前登录用户完整信息
     */
    Member getCurrentUser();
}