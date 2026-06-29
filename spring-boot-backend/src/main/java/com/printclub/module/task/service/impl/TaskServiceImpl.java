package com.printclub.module.task.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.PageUtils;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.artwork.mapper.ArtworkMapper;
import com.printclub.module.log.service.LogService;
import com.printclub.module.material.entity.MaterialLog;
import com.printclub.module.material.mapper.MaterialLogMapper;
import com.printclub.module.printer.entity.Printer;
import com.printclub.module.printer.mapper.PrinterMapper;
import com.printclub.module.task.dto.*;
import com.printclub.module.task.entity.PrintTask;
import com.printclub.module.task.mapper.TaskMapper;
import com.printclub.module.task.service.TaskService;
import com.printclub.module.user.entity.Member;
import com.printclub.module.user.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task Service 实现（E - 核心业务）
 *
 * <p>状态流转：0待审批 → 1已通过 → 3排队中 → 4打印中 → 5已完成 → 取件 → 归档</p>
 *
 * @author E
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final MemberMapper memberMapper;
    private final PrinterMapper printerMapper;
    private final MaterialLogMapper materialLogMapper;
    private final ArtworkMapper artworkMapper;
    private final LogService logService;

    private static final DateTimeFormatter TASK_ID_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ============================================
    // 申请
    // ============================================
    @Override
    public String apply(TaskApplyDTO dto) {
        String studentId = SecurityContext.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 生成任务编号：P + yyyyMMdd + 3位序号（演示用 UUID 简化）
        String dateStr = LocalDate.now().format(TASK_ID_DATE);
        String taskId = "P" + dateStr + "-" + IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();

        // 2. DTO → Entity
        PrintTask task = new PrintTask();
        task.setTaskId(taskId);
        task.setTitle(dto.getTitle());
        task.setApplicantId(studentId);
        task.setModelName(dto.getModelName());
        task.setStlFilePath(dto.getStlFilePath());
        task.setMaterialType(dto.getMaterialType());
        task.setColor(dto.getColor());
        task.setLayerHeight(dto.getLayerHeight());
        task.setInfillRate(dto.getInfillRate());
        task.setNeedSupport(dto.getNeedSupport());
        task.setPriority(dto.getPriority());
        task.setEstWeight(dto.getEstWeight());
        task.setEstTime(dto.getEstTime());
        task.setProjectId(dto.getProjectId());
        task.setStatus(PrintTask.STATUS_PENDING);

        // 3. 插入
        taskMapper.insert(task);

        log.info("任务提交成功：taskId={}, applicant={}", taskId, studentId);
        // 审计：写入 system_log（@Async 异步，不阻塞主流程）
        logService.recordCurrent("task.apply", "task", taskId, "申请打印：" + dto.getTitle());
        return taskId;
    }

    // ============================================
    // 列表查询
    // ============================================
    @Override
    public PageResult<PrintTask> myTasks(TaskQuery query) {
        String studentId = SecurityContext.getCurrentUserId();
        Page<PrintTask> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<PrintTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrintTask::getApplicantId, studentId)
                .orderByDesc(PrintTask::getApplyTime);

        // v2 修复：/task/my 默认排除"已登记过作品"的任务（task_id 不在 artwork 表里）
        // 业务语义：/task/my = "可登记任务列表"，已登记的去 /artwork/my 看
        // 避免用户重复选同一个任务登记（后端虽然有 UNIQUE 约束会报错，但前端体验更差）
        wrapper.notInSql(PrintTask::getTaskId,
                "SELECT task_id FROM artwork WHERE task_id IS NOT NULL");

        applyCommonFilters(wrapper, query);
        PageResult<PrintTask> result = PageUtils.toResult(taskMapper.selectPage(page, wrapper));
        // v2：批量注入申请人/审批人/打印机 名字
        fillTaskRelationalNames(result.getList());
        return result;
    }

    @Override
    public PageResult<PrintTask> pendingTasks(TaskQuery query) {
        Page<PrintTask> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<PrintTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrintTask::getStatus, PrintTask.STATUS_PENDING)
                .orderByAsc(PrintTask::getPriority)
                .orderByAsc(PrintTask::getApplyTime);

        applyCommonFilters(wrapper, query);
        PageResult<PrintTask> result = PageUtils.toResult(taskMapper.selectPage(page, wrapper));
        fillTaskRelationalNames(result.getList());
        return result;
    }

    @Override
    public PageResult<PrintTask> queue(TaskQuery query) {
        Page<PrintTask> page = new Page<>(query.getPage(), query.getSize());

        // 队列：状态=1(已通过未分配) + 3(排队中) + 4(打印中)，按 priority → apply_time 排序
        // v2 修复：必须包含 STATUS_APPROVED，否则审批通过后任务从"待审批"消失
        // 但又没出现在"进行中"里，看起来像凭空消失。
        // 任务完整流转：PENDING(0) → APPROVED(1) → QUEUED(3) → PRINTING(4) → DONE(5) → PICKED_UP(8)
        LambdaQueryWrapper<PrintTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PrintTask::getStatus, PrintTask.STATUS_APPROVED, PrintTask.STATUS_QUEUED, PrintTask.STATUS_PRINTING)
                .orderByAsc(PrintTask::getPriority)
                .orderByAsc(PrintTask::getApplyTime);

        applyCommonFilters(wrapper, query);
        PageResult<PrintTask> result = PageUtils.toResult(taskMapper.selectPage(page, wrapper));
        fillTaskRelationalNames(result.getList());
        return result;
    }

    @Override
    public PrintTask detail(String taskId) {
        PrintTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        // v2：详情也注入关联名字（前端展示用）
        fillTaskRelationalNames(java.util.Collections.singletonList(task));
        return task;
    }

    // ============================================
    // 关联字段填充（v2：仿 ProjectServiceImpl.fillMemberNames 模式）
    // ============================================

    /**
     * 批量把 task.applicantId/approverId/printerId 翻译成对应姓名/型号
     * 一次查 member 表 + 一次查 printer 表，N+1 → 2 次查询
     */
    private void fillTaskRelationalNames(List<PrintTask> tasks) {
        if (tasks == null || tasks.isEmpty()) return;

        // 1. 收集所有 ID
        java.util.Set<String> memberIds = new java.util.HashSet<>();
        java.util.Set<String> printerIds = new java.util.HashSet<>();
        for (PrintTask t : tasks) {
            if (t.getApplicantId() != null) memberIds.add(t.getApplicantId());
            if (t.getApproverId() != null) memberIds.add(t.getApproverId());
            if (t.getPrinterId() != null) printerIds.add(t.getPrinterId());
        }

        // 2. 批量查（用 memberMapper.selectIdNameMap 公共方法，替代原本复制粘贴的 selectBatchIds 循环）
        java.util.Map<String, String> id2name = memberMapper.selectIdNameMap(memberIds);
        java.util.Map<String, String> id2model = new java.util.HashMap<>();
        if (!printerIds.isEmpty()) {
            for (com.printclub.module.printer.entity.Printer p : printerMapper.selectBatchIds(printerIds)) {
                id2model.put(p.getPrinterId(), p.getModel());
            }
        }

        // 3. 注入
        for (PrintTask t : tasks) {
            t.setApplicantName(id2name.get(t.getApplicantId()));
            t.setApproverName(id2name.get(t.getApproverId()));
            t.setPrinterModel(id2model.get(t.getPrinterId()));
        }
    }

    private void applyCommonFilters(LambdaQueryWrapper<PrintTask> w, TaskQuery q) {
        if (q.getPrinterId() != null) {
            w.eq(PrintTask::getPrinterId, q.getPrinterId());
        }
        if (q.getApplicantId() != null) {
            w.eq(PrintTask::getApplicantId, q.getApplicantId());
        }
        // v2 修复：status 字段已定义为"逗号分隔多选字符串"，但一直没人解析
        // 后果：前端传 status=5 被忽略，后端返回所有状态的任务
        // 现在补上：解析 "5,8" → in (5, 8)
        if (q.getStatus() != null && !q.getStatus().isBlank()) {
            String[] parts = q.getStatus().split(",");
            java.util.List<Integer> statusList = new java.util.ArrayList<>();
            for (String p : parts) {
                try { statusList.add(Integer.parseInt(p.trim())); } catch (NumberFormatException ignored) {}
            }
            if (!statusList.isEmpty()) {
                w.in(PrintTask::getStatus, statusList);
            }
        }
        // v2.2 增强：keyword 现在支持 title / modelName / applicantId（学号）/ applicantName（姓名）
        // 姓名匹配通过 member 表查出学号列表后 OR 起来（避免 JOIN）
        if (q.getKeyword() != null && !q.getKeyword().isBlank()) {
            String kw = q.getKeyword().trim();
            // 姓名 → 学号列表（用现有 memberMapper，不增加新方法）
            java.util.List<String> nameMatchedIds = memberMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.printclub.module.user.entity.Member>()
                            .select(com.printclub.module.user.entity.Member::getStudentId)
                            .like(com.printclub.module.user.entity.Member::getName, kw)
            ).stream()
                    .map(com.printclub.module.user.entity.Member::getStudentId)
                    .collect(java.util.stream.Collectors.toList());

            w.and(x -> x
                    .like(PrintTask::getTitle, kw)
                    .or().like(PrintTask::getModelName, kw)
                    .or().like(PrintTask::getApplicantId, kw)
                    .or().in(!nameMatchedIds.isEmpty(), PrintTask::getApplicantId, nameMatchedIds)
            );
        }
    }

    // ============================================
    // 审批
    // ============================================
    @Override
    public void approve(String taskId, ApproveDTO dto) {
        PrintTask task = mustGetTask(taskId);
        if (task.getStatus() != PrintTask.STATUS_PENDING) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅待审批状态可通过");
        }

        // v2 修复：先进入 STATUS_APPROVED(1)，分配打印机时再变 STATUS_QUEUED(3)
        task.setStatus(PrintTask.STATUS_APPROVED);
        task.setApproverId(SecurityContext.getCurrentUserId());
        task.setApproveTime(LocalDateTime.now());
        task.setApproveComment(dto.getApproveComment());
        taskMapper.updateById(task);

        logService.recordCurrent("task.approve", "task", taskId,
                "审批通过：" + (StrUtil.isBlank(dto.getApproveComment()) ? "无备注" : dto.getApproveComment()));
    }

    @Override
    public void reject(String taskId, RejectDTO dto) {
        PrintTask task = mustGetTask(taskId);
        if (task.getStatus() != PrintTask.STATUS_PENDING) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅待审批状态可驳回");
        }

        task.setStatus(PrintTask.STATUS_REJECTED);
        task.setApproverId(SecurityContext.getCurrentUserId());
        task.setApproveTime(LocalDateTime.now());
        task.setApproveComment(dto.getApproveComment());
        taskMapper.updateById(task);

        logService.recordCurrent("task.reject", "task", taskId,
                "驳回：" + (StrUtil.isBlank(dto.getApproveComment()) ? "无原因" : dto.getApproveComment()));
    }

    // ============================================
    // 分配 / 开始 / 完成
    // ============================================
    @Override
    public void assignPrinter(String taskId, AssignPrinterDTO dto) {
        PrintTask task = mustGetTask(taskId);
        // v2 修复：已通过(1) 或 排队中(3) 都可以分配（支持换打印机）
        if (task.getStatus() != PrintTask.STATUS_APPROVED && task.getStatus() != PrintTask.STATUS_QUEUED) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅已通过/排队中状态可分配打印机");
        }

        // 校验打印机
        Printer printer = printerMapper.selectById(dto.getPrinterId());
        if (printer == null) {
            throw new BusinessException(ResultCode.PRINTER_NOT_FOUND);
        }
        if (printer.getStatus() != null && printer.getStatus() != 1) {
            throw new BusinessException(ResultCode.PRINTER_BROKEN);
        }

        // v2 重构：用 taskMapper.selectBusyPrinterIds() 公共方法判断该打印机是否被占
        // （包含 STATUS_QUEUED(3) + STATUS_PRINTING(4) — 已分配未打印也算占用）
        if (taskMapper.selectBusyPrinterIds().contains(dto.getPrinterId())) {
            throw new BusinessException(ResultCode.PRINTER_BUSY);
        }

        task.setPrinterId(dto.getPrinterId());
        task.setStatus(PrintTask.STATUS_QUEUED);  // 已通过 → 排队中
        taskMapper.updateById(task);

        logService.recordCurrent("task.assign", "task", taskId, "分配打印机：" + dto.getPrinterId());
    }

    @Override
    public void startPrint(String taskId) {
        PrintTask task = mustGetTask(taskId);
        if (task.getStatus() != PrintTask.STATUS_QUEUED) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅排队中状态可开始打印");
        }
        if (task.getPrinterId() == null) {
            throw new BusinessException(ResultCode.TASK_NO_PRINTER, "请先分配打印机");
        }

        task.setStatus(PrintTask.STATUS_PRINTING);
        taskMapper.updateById(task);

        logService.recordCurrent("task.start", "task", taskId, "开始打印");
    }

    /**
     * 完成打印 — 业务最复杂的一步
     *
     * <p>事务内完成：</p>
     * <ol>
     *   <li>更新任务状态（4 → 5）</li>
     *   <li>扣减耗材库存（material_log 写一条消耗记录）</li>
     *   <li>累计申请人的打印次数</li>
     *   <li>累计打印机使用时长</li>
     *   <li>v2 修复：不再自动归档作品库（与手动登记冲突，由用户主动登记）</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishPrint(String taskId, FinishPrintDTO dto) {
        PrintTask task = mustGetTask(taskId);
        if (task.getStatus() != PrintTask.STATUS_PRINTING) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅打印中状态可标记完成");
        }

        // 1. 更新任务
        task.setStatus(PrintTask.STATUS_DONE);
        task.setActualWeight(dto.getActualWeight());
        task.setActualTime(dto.getActualTime());
        task.setQualityScore(dto.getQualityScore());
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);

        // 2. 扣减耗材库存
        BigDecimal currentBalance = getCurrentBalance(task.getMaterialType(), task.getColor());
        // ⚠️ v2.2 修复：null 表示"该材料+颜色没有任何库存记录"（从未入库过），
        //          0 表示"库存恰好为 0"，要区分两种情况给不同提示
        if (currentBalance == null) {
            throw new BusinessException(ResultCode.TASK_INSUFFICIENT_STOCK,
                    "未找到耗材库存记录：" + task.getMaterialType() + " " + task.getColor() + "，请先入库");
        }
        BigDecimal newBalance = currentBalance.subtract(dto.getActualWeight());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.TASK_INSUFFICIENT_STOCK,
                    "耗材库存不足：当前 " + currentBalance + "g，需要 " + dto.getActualWeight() + "g");
        }

        MaterialLog logEntry = new MaterialLog();
        logEntry.setMaterialType(task.getMaterialType());
        logEntry.setColor(task.getColor());
        logEntry.setWeightChange(dto.getActualWeight().negate());  // 负数
        logEntry.setBalance(newBalance);
        logEntry.setOperationType(2);  // 消耗
        logEntry.setRelatedTaskId(taskId);
        logEntry.setOperatorId(SecurityContext.getCurrentUserId());
        logEntry.setRemark("任务完成自动扣减");
        materialLogMapper.insert(logEntry);

        // 3. 累计申请人打印次数
        Member applicant = memberMapper.selectById(task.getApplicantId());
        if (applicant != null) {
            applicant.setTotalPrints((applicant.getTotalPrints() == null ? 0 : applicant.getTotalPrints()) + 1);
            memberMapper.updateById(applicant);
        }

        // 4. 累计打印机时长
        Printer printer = printerMapper.selectById(task.getPrinterId());
        if (printer != null && dto.getActualTime() != null) {
            BigDecimal hours = BigDecimal.valueOf(dto.getActualTime())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            printer.setTotalPrintHours(
                    (printer.getTotalPrintHours() == null ? BigDecimal.ZERO : printer.getTotalPrintHours())
                            .add(hours)
            );
            printerMapper.updateById(printer);
        }

        // 4. v2 修复：去掉"完成打印自动归档作品库"逻辑
        //    原因：完成打印和登记作品是两个独立动作，强制自动上传会和用户手动登记流程
        //    冲突（重复创建、用户没机会填心得/选照片）。
        //    现在 task.finish 不创建 artwork，作品完全由用户通过"登记作品"入口手动上传。

        log.info("任务完成：taskId={}, applicant={}, 消耗耗材={}g",
                taskId, task.getApplicantId(), dto.getActualWeight());
        logService.recordCurrent("task.finish", "task", taskId,
                "完成打印：实耗 " + dto.getActualWeight() + "g，质量分 " + dto.getQualityScore());
    }

    /**
     * 获取指定耗材+颜色的当前库存余额
     * ⚠️ v2.2 修复：返回 null 表示"该材料+颜色从未入库过"（区别于"库存恰好为 0"）
     *
     * @return 当前库存余额；如果没有任何 material_log 记录则返回 null
     */
    private BigDecimal getCurrentBalance(String materialType, String color) {
        MaterialLog latest = materialLogMapper.selectOne(
                new LambdaQueryWrapper<MaterialLog>()
                        .eq(MaterialLog::getMaterialType, materialType)
                        .eq(MaterialLog::getColor, color)
                        .orderByDesc(MaterialLog::getLogId)
                        .last("LIMIT 1")
        );
        return latest == null ? null : latest.getBalance();
    }

    // ============================================
    // 取件 / 取消
    // ============================================
    @Override
    public void pickup(String taskId, PickupDTO dto) {
        String studentId = SecurityContext.getCurrentUserId();
        PrintTask task = mustGetTask(taskId);
        if (!task.getApplicantId().equals(studentId)) {
            throw new BusinessException(ResultCode.TASK_NOT_YOURS);
        }
        if (task.getStatus() != PrintTask.STATUS_DONE) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅已完成状态可签收");
        }
        if (task.getPickupTime() != null) {
            throw new BusinessException(ResultCode.TASK_ALREADY_PICKED_UP);
        }

        task.setPickupTime(LocalDateTime.now());
        // v2 修复：pickup 后 status 变 STATUS_PICKED_UP(8)（从 5 已完成 → 8 已取件）
        task.setStatus(PrintTask.STATUS_PICKED_UP);
        if (dto.getQualityScore() != null) {
            task.setQualityScore(dto.getQualityScore());
        }
        taskMapper.updateById(task);

        logService.recordCurrent("task.pickup", "task", taskId, "取件完成");
    }

    @Override
    public void cancel(String taskId) {
        String studentId = SecurityContext.getCurrentUserId();
        PrintTask task = mustGetTask(taskId);
        if (!task.getApplicantId().equals(studentId)) {
            throw new BusinessException(ResultCode.TASK_NOT_YOURS);
        }
        if (task.getStatus() != PrintTask.STATUS_PENDING
                && task.getStatus() != PrintTask.STATUS_QUEUED) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅待审批/排队中可取消");
        }

        task.setStatus(PrintTask.STATUS_CANCELLED);
        taskMapper.updateById(task);

        logService.recordCurrent("task.cancel", "task", taskId, "用户取消任务");
    }

    /**
     * 修改任务（部分字段）
     * v2.2 新增：让技术骨干及以上角色可以修改 priority 等非流转字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String taskId, UpdateTaskDTO dto) {
        PrintTask task = mustGetTask(taskId);

        // 优先级
        if (dto.getPriority() != null) {
            // 已完成/已取消的任务不能改优先级
            if (task.getStatus() == PrintTask.STATUS_DONE
                    || task.getStatus() == PrintTask.STATUS_CANCELLED
                    || task.getStatus() == PrintTask.STATUS_PICKED_UP) {
                throw new BusinessException(ResultCode.TASK_STATUS_INVALID,
                        "已完成/已取件/已取消的任务不能修改优先级");
            }
            Integer oldPriority = task.getPriority();
            task.setPriority(dto.getPriority());
            logService.recordCurrent("task.updatePriority", "task", taskId,
                    "修改优先级：" + oldPriority + " → " + dto.getPriority());
        }

        taskMapper.updateById(task);
    }

    // ============================================
    // 统计
    // ============================================
    @Override
    public Object stats() {
        // 简单版：返回各状态的任务数
        Map<String, Object> result = new HashMap<>();
        for (int s = 0; s <= 6; s++) {
            long count = taskMapper.selectCount(
                    new LambdaQueryWrapper<PrintTask>().eq(PrintTask::getStatus, s));
            result.put("status_" + s, count);
        }
        return result;
    }

    // ============================================
    // 工具
    // ============================================
    private PrintTask mustGetTask(String taskId) {
        PrintTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return task;
    }
}