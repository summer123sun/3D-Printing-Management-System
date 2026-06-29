package com.printclub.module.user.service;

import com.printclub.common.result.PageResult;
import com.printclub.module.user.dto.*;
import com.printclub.module.user.entity.Member;

/**
 * 用户 Service
 *
 * @author A
 */
public interface UserService {

    /**
     * 成员列表（分页 + 关键字 + 角色筛选）
     */
    PageResult<Member> list(Integer page, Integer size, String keyword, Integer role);

    /**
     * 修改角色（仅社长）
     */
    void updateRole(String studentId, UpdateRoleDTO dto);

    /**
     * 修改技能等级
     */
    void updateSkill(String studentId, UpdateSkillDTO dto);

    /**
     * 个人统计
     */
    UserStatsVO stats(String studentId);

    /**
     * 根据学号查用户
     */
    Member getByStudentId(String studentId);

    /**
     * 修改当前登录用户个人信息
     */
    void updateInfo(UpdateUserInfoDTO dto);

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordDTO dto);

    /**
     * 新增成员（仅社长）
     * @return 新增成员的学号
     */
    String add(AddMemberDTO dto);
}
