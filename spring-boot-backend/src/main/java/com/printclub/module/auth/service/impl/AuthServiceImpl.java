package com.printclub.module.auth.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.JwtUtil;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.auth.dto.LoginDTO;
import com.printclub.module.auth.dto.LoginVO;
import com.printclub.module.auth.service.AuthService;
import com.printclub.module.log.service.LogService;
import com.printclub.module.user.entity.Member;
import com.printclub.module.user.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证 Service 实现
 *
 * <p>密码用 BCrypt 校验（v2 已废弃 MD5）</p>
 *
 * @author D
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final LogService logService;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查用户
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getStudentId, dto.getStudentId())
        );
        if (member == null) {
            log.warn("登录失败 - 学号 {} 不存在", dto.getStudentId());
            throw new BusinessException(ResultCode.LOGIN_FAIL, "账号不存在，请检查学号");
        }

        // 2. 校验状态
        if (member.getStatus() != null && member.getStatus() == 2) {
            log.warn("登录失败 - 学号 {} 账号已禁用", dto.getStudentId());
            throw new BusinessException(ResultCode.LOGIN_FAIL, "账号已被禁用，请联系管理员");
        }

        // 3. 校验密码（BCrypt）
        if (!BCrypt.checkpw(dto.getPassword(), member.getPassword())) {
            log.warn("登录失败 - 学号 {} 密码错误", dto.getStudentId());
            throw new BusinessException(ResultCode.LOGIN_FAIL, "密码错误，请重新输入");
        }

        // 4. 签发 JWT
        String token = jwtUtil.generate(member.getStudentId(), member.getRole());

        // 5. 清空密码字段（不返回给前端）
        member.setPassword(null);

        log.info("登录成功 - 学号 {} 角色 {}", member.getStudentId(), member.getRole());
        // 审计：登录成功也写日志（IP 自动从 RequestContextHolder 取）
        logService.recordCurrent("auth.login", "user", member.getStudentId(),
                "登录成功，角色 role=" + member.getRole());
        return new LoginVO(token, member);
    }

    @Override
    public Member getCurrentUser() {
        String studentId = SecurityContext.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getStudentId, studentId)
        );
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        // 不返回密码
        member.setPassword(null);
        return member;
    }
}