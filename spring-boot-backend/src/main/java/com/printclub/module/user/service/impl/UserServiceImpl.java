package com.printclub.module.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.PageUtils;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.user.dto.*;
import com.printclub.module.user.entity.Member;
import com.printclub.module.user.mapper.MemberMapper;
import com.printclub.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户 Service 实现
 *
 * @author A
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final MemberMapper memberMapper;

    @Override
    public PageResult<Member> list(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        // 关键字搜索：按学号或姓名模糊匹配
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(Member::getStudentId, keyword)
                    .or()
                    .like(Member::getName, keyword)
            );
        }
        wrapper.orderByDesc(Member::getCreateTime);

        Page<Member> mpPage = memberMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10),
                wrapper
        );

        // 清除密码字段
        mpPage.getRecords().forEach(m -> m.setPassword(null));

        return PageUtils.toResult(mpPage);
    }

    @Override
    public void updateRole(String studentId, UpdateRoleDTO dto) {
        Member member = findMemberOrThrow(studentId);
        member.setRole(dto.getRole());
        memberMapper.updateById(member);
        log.info("成员 {} 角色变更为 {}", studentId, dto.getRole());
    }

    @Override
    public void updateSkill(String studentId, UpdateSkillDTO dto) {
        Member member = findMemberOrThrow(studentId);
        member.setSkillLevel(dto.getSkillLevel());
        memberMapper.updateById(member);
        log.info("成员 {} 技能等级变更为 {}", studentId, dto.getSkillLevel());
    }

    @Override
    public UserStatsVO stats(String studentId) {
        // 确认用户存在
        findMemberOrThrow(studentId);

        // 查询累计打印次数（从 member 表 total_prints 字段取）
        Member member = memberMapper.selectById(studentId);

        // 查询参与项目数
        Long totalProjects = memberMapper.countProjectsByStudentId(studentId);

        // 查询作品数
        Long totalArtworks = memberMapper.countArtworksByStudentId(studentId);

        return new UserStatsVO(
                member.getTotalPrints() != null ? member.getTotalPrints() : 0,
                totalProjects != null ? totalProjects.intValue() : 0,
                totalArtworks != null ? totalArtworks.intValue() : 0
        );
    }

    @Override
    public Member getByStudentId(String studentId) {
        Member member = findMemberOrThrow(studentId);
        // 不返回密码
        member.setPassword(null);
        return member;
    }

    @Override
    @Transactional
    public void updateInfo(UpdateUserInfoDTO dto) {
        String studentId = SecurityContext.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }

        Member member = findMemberOrThrow(studentId);
        if (dto.getPhone() != null) {
            member.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            member.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null) {
            member.setAvatar(dto.getAvatar());
        }
        memberMapper.updateById(member);
        log.info("用户 {} 更新个人信息", studentId);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        String studentId = SecurityContext.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }

        Member member = findMemberOrThrow(studentId);

        // 校验旧密码
        if (!BCrypt.checkpw(dto.getOldPassword(), member.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "旧密码错误");
        }

        // 更新为新密码
        member.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        memberMapper.updateById(member);
        log.info("用户 {} 修改密码成功", studentId);
    }

    // ============== 私有方法 ==============

    private Member findMemberOrThrow(String studentId) {
        Member member = memberMapper.selectById(studentId);
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return member;
    }
}
