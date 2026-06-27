package com.printclub.module.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.PageUtils;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.log.service.LogService;
import com.printclub.module.project.dto.*;
import com.printclub.module.project.entity.Project;
import com.printclub.module.project.entity.ProjectFile;
import com.printclub.module.project.entity.ProjectMember;
import com.printclub.module.project.entity.ProjectProgress;
import com.printclub.module.project.mapper.ProjectFileMapper;
import com.printclub.module.project.mapper.ProjectMapper;
import com.printclub.module.project.mapper.ProjectMemberMapper;
import com.printclub.module.project.mapper.ProjectProgressMapper;
import com.printclub.module.project.service.ProjectService;
import com.printclub.module.task.entity.PrintTask;
import com.printclub.module.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project Service 实现（E - 核心业务）
 *
 * <p>权限约定：</p>
 * <ul>
 *   <li>创建项目：社长/技术骨干</li>
 *   <li>修改项目/成员/阶段：项目负责人</li>
 *   <li>完成/取消项目：项目负责人</li>
 * </ul>
 *
 * @author E
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper memberMapper;
    private final ProjectProgressMapper progressMapper;
    private final ProjectFileMapper fileMapper;
    private final TaskMapper taskMapper;
    private final com.printclub.module.user.mapper.MemberMapper memberInfoMapper;
    private final LogService logService;

    // ============================================
    // CRUD
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer create(ProjectCreateDTO dto) {
        String studentId = SecurityContext.getCurrentUserId();

        // 1. 创建项目
        Project project = new Project();
        project.setProjectName(dto.getProjectName());
        project.setProjectType(dto.getProjectType());
        project.setLeaderId(
            (dto.getLeaderId() != null && !dto.getLeaderId().isBlank())
                ? dto.getLeaderId() : studentId);
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setBudget(dto.getBudget());
        project.setDescription(dto.getDescription());
        project.setDeliverables(dto.getDeliverables());
        project.setCoverImage(dto.getCoverImage());
        project.setStatus(Project.STATUS_PREPARING);
        projectMapper.insert(project);

        // 2. 创建阶段
        if (dto.getStages() != null) {
            for (StageDTO s : dto.getStages()) {
                ProjectProgress stage = new ProjectProgress();
                stage.setProjectId(project.getProjectId());
                stage.setStageName(s.getStageName());
                stage.setStageOrder(s.getStageOrder());
                stage.setDescription(s.getDescription());
                stage.setResponsibleId(s.getResponsibleId());
                stage.setStatus(Project.STAGE_STATUS_PENDING);
                progressMapper.insert(stage);
            }
        }

        // 3. 自动把负责人加入成员列表
        ProjectMember leader = new ProjectMember();
        leader.setProjectId(project.getProjectId());
        leader.setMemberId(project.getLeaderId());
        leader.setRoleInProject(Project.ROLE_LEADER);
        leader.setContribution("项目负责人");
        leader.setStatus(Project.MEMBER_STATUS_ACTIVE);
        memberMapper.insert(leader);

        // 4. 添加初始成员
        if (dto.getInitialMembers() != null) {
            for (AddMemberDTO m : dto.getInitialMembers()) {
                ProjectMember pm = new ProjectMember();
                pm.setProjectId(project.getProjectId());
                pm.setMemberId(m.getMemberId());
                pm.setRoleInProject(m.getRoleInProject());
                pm.setContribution(m.getContribution());
                pm.setStatus(Project.MEMBER_STATUS_ACTIVE);
                memberMapper.insert(pm);
            }
        }

        log.info("项目创建成功：projectId={}, name={}", project.getProjectId(), project.getProjectName());
        logService.recordCurrent("project.create", "project", String.valueOf(project.getProjectId()),
                "创建项目：「" + project.getProjectName() + "」");
        return project.getProjectId();
    }

    @Override
    public PageResult<?> list(ProjectQuery query) {
        Page<Project> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Project> w = new LambdaQueryWrapper<>();
        if (query.getStatus() != null) w.eq(Project::getStatus, query.getStatus());
        if (query.getProjectType() != null) w.eq(Project::getProjectType, query.getProjectType());
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            w.like(Project::getProjectName, query.getKeyword());
        }

        // scope=mine：仅我参与的
        if ("mine".equalsIgnoreCase(query.getScope())) {
            String studentId = SecurityContext.getCurrentUserId();
            // 先查我参与的项目 ID 列表
            List<ProjectMember> myMemberships = memberMapper.selectList(
                    new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getMemberId, studentId));
            if (myMemberships.isEmpty()) {
                return PageResult.of(java.util.Collections.emptyList(), 0, query.getPage(), query.getSize());
            }
            List<Integer> myProjectIds = myMemberships.stream()
                    .map(ProjectMember::getProjectId).toList();
            w.in(Project::getProjectId, myProjectIds);
        }

        w.orderByDesc(Project::getCreateTime);
        PageResult<Project> result = PageUtils.toResult(projectMapper.selectPage(page, w));

        // v2：批量填充负责人姓名（前端表格"负责人"列展示用，跟 AppHeader 显示姓名一致）
        fillLeaderNames(result.getList());

        return result;
    }

    /**
     * v2 优化：批量把 Project.leaderId 翻译成 leaderName（前端表格展示用）
     * 类似 fillMemberNames() / fillStageResponsibleNames() 的批量翻译模式
     */
    private void fillLeaderNames(List<Project> projects) {
        if (projects == null || projects.isEmpty()) return;
        List<String> ids = projects.stream()
                .map(Project::getLeaderId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return;
        List<com.printclub.module.user.entity.Member> infos = memberInfoMapper.selectBatchIds(ids);
        java.util.Map<String, String> id2name = infos.stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.printclub.module.user.entity.Member::getStudentId,
                        com.printclub.module.user.entity.Member::getName,
                        (a, b) -> a));
        projects.forEach(p -> p.setLeaderName(id2name.get(p.getLeaderId())));
    }

    @Override
    public ProjectDetailVO detail(Integer projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        List<ProjectMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));

        // 关联查询真实姓名（前端成员表需要显示）
        fillMemberNames(members);

        List<ProjectProgress> stages = progressMapper.selectList(
                new LambdaQueryWrapper<ProjectProgress>()
                        .eq(ProjectProgress::getProjectId, projectId)
                        .orderByAsc(ProjectProgress::getStageOrder));

        // 阶段负责人姓名
        fillStageResponsibleNames(stages);

        List<ProjectFile> files = fileMapper.selectList(
                new LambdaQueryWrapper<ProjectFile>().eq(ProjectFile::getProjectId, projectId));

        List<PrintTask> relatedTasks = taskMapper.selectList(
                new LambdaQueryWrapper<PrintTask>().eq(PrintTask::getProjectId, projectId));

        return new ProjectDetailVO(project, members, stages, files, relatedTasks);
    }

    private void fillMemberNames(List<ProjectMember> members) {
        if (members == null || members.isEmpty()) return;
        List<String> ids = members.stream().map(ProjectMember::getMemberId).toList();
        List<com.printclub.module.user.entity.Member> infos = memberInfoMapper.selectBatchIds(ids);
        java.util.Map<String, String> id2name = infos.stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.printclub.module.user.entity.Member::getStudentId,
                        com.printclub.module.user.entity.Member::getName,
                        (a, b) -> a));
        members.forEach(m -> m.setMemberName(id2name.get(m.getMemberId())));
    }

    private void fillStageResponsibleNames(List<ProjectProgress> stages) {
        if (stages == null || stages.isEmpty()) return;
        List<String> ids = stages.stream()
                .map(ProjectProgress::getResponsibleId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return;
        List<com.printclub.module.user.entity.Member> infos = memberInfoMapper.selectBatchIds(ids);
        java.util.Map<String, String> id2name = infos.stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.printclub.module.user.entity.Member::getStudentId,
                        com.printclub.module.user.entity.Member::getName,
                        (a, b) -> a));
        stages.forEach(s -> s.setResponsibleName(id2name.get(s.getResponsibleId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer projectId, ProjectCreateDTO dto) {
        mustBeLeader(projectId);
        Project project = mustGetProject(projectId);
        if (project.getStatus() == Project.STATUS_DONE || project.getStatus() == Project.STATUS_CANCELLED) {
            throw new BusinessException(ResultCode.PROJECT_STATUS_INVALID, "已结束的项目不可修改");
        }

        project.setProjectName(dto.getProjectName());
        project.setProjectType(dto.getProjectType());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setBudget(dto.getBudget());
        project.setDescription(dto.getDescription());
        project.setDeliverables(dto.getDeliverables());
        project.setCoverImage(dto.getCoverImage());
        projectMapper.updateById(project);
    }

    @Override
    public void complete(Integer projectId) {
        mustBeLeader(projectId);
        Project project = mustGetProject(projectId);
        project.setStatus(Project.STATUS_DONE);
        project.setActualEndDate(java.time.LocalDate.now());
        projectMapper.updateById(project);
        logService.recordCurrent("project.complete", "project", String.valueOf(projectId),
                "完成项目：「" + project.getProjectName() + "」");
    }

    @Override
    public void cancel(Integer projectId) {
        mustBeLeader(projectId);
        Project project = mustGetProject(projectId);
        project.setStatus(Project.STATUS_CANCELLED);
        project.setActualEndDate(java.time.LocalDate.now());
        projectMapper.updateById(project);
        logService.recordCurrent("project.cancel", "project", String.valueOf(projectId),
                "取消项目：「" + project.getProjectName() + "」");
    }

    // ============================================
    // 成员管理
    // ============================================

    @Override
    public void addMember(Integer projectId, AddMemberDTO dto) {
        mustBeLeader(projectId);

        // 检查是否已在项目中
        Long exists = memberMapper.selectCount(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getMemberId, dto.getMemberId()));
        if (exists > 0) {
            throw new BusinessException(ResultCode.PROJECT_MEMBER_EXISTS);
        }

        ProjectMember pm = new ProjectMember();
        pm.setProjectId(projectId);
        pm.setMemberId(dto.getMemberId());
        pm.setRoleInProject(dto.getRoleInProject());
        pm.setContribution(dto.getContribution());
        pm.setStatus(Project.MEMBER_STATUS_ACTIVE);
        memberMapper.insert(pm);
        logService.recordCurrent("project.addMember", "project", String.valueOf(projectId),
                "添加成员：" + dto.getMemberId());
    }

    @Override
    public void removeMember(Integer projectId, String memberId) {
        mustBeLeader(projectId);

        // 不能移除负责人
        Project project = mustGetProject(projectId);
        if (project.getLeaderId().equals(memberId)) {
            throw new BusinessException(ResultCode.PROJECT_STATUS_INVALID, "不能移除项目负责人");
        }

        memberMapper.delete(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getMemberId, memberId));
        logService.recordCurrent("project.removeMember", "project", String.valueOf(projectId),
                "移除成员：" + memberId);
    }

    @Override
    public void updateMemberRole(Integer projectId, String memberId, Integer roleInProject) {
        mustBeLeader(projectId);
        ProjectMember pm = memberMapper.selectOne(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getMemberId, memberId));
        if (pm == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND, "成员不在此项目中");
        }
        pm.setRoleInProject(roleInProject);
        memberMapper.updateById(pm);
    }

    // ============================================
    // 阶段管理
    // ============================================

    @Override
    public Integer addStage(Integer projectId, StageDTO dto) {
        mustBeLeader(projectId);

        ProjectProgress stage = new ProjectProgress();
        stage.setProjectId(projectId);
        stage.setStageName(dto.getStageName());
        stage.setStageOrder(dto.getStageOrder());
        stage.setDescription(dto.getDescription());
        stage.setResponsibleId(dto.getResponsibleId());
        stage.setStatus(Project.STAGE_STATUS_PENDING);
        progressMapper.insert(stage);
        return stage.getProgressId();
    }

    @Override
    public void updateStage(Integer projectId, Integer progressId, StageDTO dto) {
        mustBeLeader(projectId);
        ProjectProgress stage = mustGetStage(projectId, progressId);
        stage.setStageName(dto.getStageName());
        stage.setStageOrder(dto.getStageOrder());
        stage.setDescription(dto.getDescription());
        stage.setResponsibleId(dto.getResponsibleId());
        progressMapper.updateById(stage);
    }

    @Override
    public void updateStageStatus(Integer projectId, Integer progressId, Integer status) {
        ProjectProgress stage = mustGetStage(projectId, progressId);

        // 允许任意状态切换：PENDING(0) / RUNNING(1) / DONE(2) 之间可互转
        // 但首次进入 RUNNING 时写开始时间；首次进入 DONE 时写结束时间
        if (status == Project.STAGE_STATUS_RUNNING && stage.getStartTime() == null) {
            stage.setStartTime(LocalDateTime.now());
        } else if (status == Project.STAGE_STATUS_DONE && stage.getEndTime() == null) {
            stage.setEndTime(LocalDateTime.now());
        } else if (status == Project.STAGE_STATUS_PENDING) {
            // 重置为未开始时，清掉时间（让用户可以重新开始）
            stage.setStartTime(null);
            stage.setEndTime(null);
        }
        stage.setStatus(status);
        progressMapper.updateById(stage);

        // 阶段开始时，如果项目还在筹备中，自动转为进行中
        if (status == Project.STAGE_STATUS_RUNNING) {
            Project project = projectMapper.selectById(projectId);
            if (project != null && project.getStatus() == Project.STATUS_PREPARING) {
                project.setStatus(Project.STATUS_RUNNING);
                projectMapper.updateById(project);
            }
        }
    }

    @Override
    public void deleteStage(Integer projectId, Integer progressId) {
        mustBeLeader(projectId);
        ProjectProgress stage = mustGetStage(projectId, progressId);
        if (stage.getStatus() == Project.STAGE_STATUS_DONE) {
            throw new BusinessException(ResultCode.PROJECT_STATUS_INVALID, "已完成的阶段不可删除");
        }
        progressMapper.deleteById(progressId);
    }

    // ============================================
    // 工具方法
    // ============================================

    private Project mustGetProject(Integer projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    private ProjectProgress mustGetStage(Integer projectId, Integer progressId) {
        ProjectProgress stage = progressMapper.selectById(progressId);
        if (stage == null || !stage.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND, "阶段不存在");
        }
        return stage;
    }

    /** 校验当前用户是项目负责人 */
    private void mustBeLeader(Integer projectId) {
        String studentId = SecurityContext.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }
        if (!project.getLeaderId().equals(studentId)) {
            // 社长可绕过此校验
            Integer role = SecurityContext.getCurrentRole();
            if (role == null || role != 1) {
                throw new BusinessException(ResultCode.PROJECT_NOT_OWNER);
            }
        }
    }
}