package com.printclub.module.project.service;

import com.printclub.common.result.PageResult;
import com.printclub.module.project.dto.*;

/**
 * 项目 Service（E - 核心业务）
 *
 * @author E
 */
public interface ProjectService {

    /**
     * 创建项目（含初始阶段和成员）
     */
    Integer create(ProjectCreateDTO dto);

    /**
     * 项目列表（分页）
     *
     * @param query.query.scope = "mine" 仅我参与的；不传则全部
     */
    PageResult<?> list(ProjectQuery query);

    /**
     * 项目详情（聚合 4 张表）
     */
    ProjectDetailVO detail(Integer projectId);

    /**
     * 修改项目
     */
    void update(Integer projectId, ProjectCreateDTO dto);

    /**
     * 标记项目完成
     */
    void complete(Integer projectId);

    /**
     * 取消项目
     */
    void cancel(Integer projectId);

    /**
     * 添加成员
     */
    void addMember(Integer projectId, AddMemberDTO dto);

    /**
     * 移除成员
     */
    void removeMember(Integer projectId, String memberId);

    /**
     * 修改成员角色
     */
    void updateMemberRole(Integer projectId, String memberId, Integer roleInProject);

    /**
     * 添加阶段
     */
    Integer addStage(Integer projectId, StageDTO dto);

    /**
     * 修改阶段
     */
    void updateStage(Integer projectId, Integer progressId, StageDTO dto);

    /**
     * 更新阶段状态（如：未开始 → 进行中 → 已完成）
     */
    void updateStageStatus(Integer projectId, Integer progressId, Integer status);

    /**
     * 删除阶段
     */
    void deleteStage(Integer projectId, Integer progressId);
}