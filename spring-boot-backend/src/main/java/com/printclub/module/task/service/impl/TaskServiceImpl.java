package com.printclub.module.task.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.PageUtils;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.artwork.entity.Artwork;
import com.printclub.module.artwork.mapper.ArtworkMapper;
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

        applyCommonFilters(wrapper, query);
        return PageUtils.toResult(taskMapper.selectPage(page, wrapper));
    }

    @Override
    public PageResult<PrintTask> pendingTasks(TaskQuery query) {
        Page<PrintTask> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<PrintTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrintTask::getStatus, PrintTask.STATUS_PENDING)
                .orderByAsc(PrintTask::getPriority)
                .orderByAsc(PrintTask::getApplyTime);

        applyCommonFilters(wrapper, query);
        return PageUtils.toResult(taskMapper.selectPage(page, wrapper));
    }

    @Override
    public PageResult<PrintTask> queue(TaskQuery query) {
        Page<PrintTask> page = new Page<>(query.getPage(), query.getSize());

        // 队列：状态=3(排队中) 或 4(打印中)，按 priority → apply_time 排序
        LambdaQueryWrapper<PrintTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PrintTask::getStatus, PrintTask.STATUS_QUEUED, PrintTask.STATUS_PRINTING)
                .orderByAsc(PrintTask::getPriority)
                .orderByAsc(PrintTask::getApplyTime);

        applyCommonFilters(wrapper, query);
        return PageUtils.toResult(taskMapper.selectPage(page, wrapper));
    }

    @Override
    public PrintTask detail(String taskId) {
        PrintTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private void applyCommonFilters(LambdaQueryWrapper<PrintTask> w, TaskQuery q) {
        if (q.getPrinterId() != null) {
            w.eq(PrintTask::getPrinterId, q.getPrinterId());
        }
        if (q.getApplicantId() != null) {
            w.eq(PrintTask::getApplicantId, q.getApplicantId());
        }
        if (q.getKeyword() != null && !q.getKeyword().isBlank()) {
            w.and(x -> x.like(PrintTask::getTitle, q.getKeyword())
                    .or().like(PrintTask::getModelName, q.getKeyword()));
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

        task.setStatus(PrintTask.STATUS_QUEUED);  // 通过后直接进入排队
        task.setApproverId(SecurityContext.getCurrentUserId());
        task.setApproveTime(LocalDateTime.now());
        task.setApproveComment(dto.getApproveComment());
        taskMapper.updateById(task);
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
    }

    // ============================================
    // 分配 / 开始 / 完成
    // ============================================
    @Override
    public void assignPrinter(String taskId, AssignPrinterDTO dto) {
        PrintTask task = mustGetTask(taskId);
        if (task.getStatus() != PrintTask.STATUS_QUEUED) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID, "仅排队中状态可分配打印机");
        }

        // 校验打印机
        Printer printer = printerMapper.selectById(dto.getPrinterId());
        if (printer == null) {
            throw new BusinessException(ResultCode.PRINTER_NOT_FOUND);
        }
        if (printer.getStatus() != null && printer.getStatus() != 1) {
            throw new BusinessException(ResultCode.PRINTER_BROKEN);
        }

        // 校验该打印机是否已被占用
        Long busy = taskMapper.selectCount(
                new LambdaQueryWrapper<PrintTask>()
                        .eq(PrintTask::getPrinterId, dto.getPrinterId())
                        .eq(PrintTask::getStatus, PrintTask.STATUS_PRINTING)
        );
        if (busy > 0) {
            throw new BusinessException(ResultCode.PRINTER_BUSY);
        }

        task.setPrinterId(dto.getPrinterId());
        taskMapper.updateById(task);
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
     *   <li>自动归档作品库</li>
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

        // 5. 自动归档作品库（v2：去掉了 likes 和 print_params）
        Artwork artwork = new Artwork();
        artwork.setTaskId(taskId);
        artwork.setAuthorId(task.getApplicantId());
        artwork.setArtworkName(task.getTitle());
        artwork.setIsRecommended(0);
        artwork.setViewCount(0);
        artworkMapper.insert(artwork);

        log.info("任务完成：taskId={}, applicant={}, 消耗耗材={}g",
                taskId, task.getApplicantId(), dto.getActualWeight());
    }

    /**
     * 获取指定耗材+颜色的当前库存余额
     */
    private BigDecimal getCurrentBalance(String materialType, String color) {
        MaterialLog latest = materialLogMapper.selectOne(
                new LambdaQueryWrapper<MaterialLog>()
                        .eq(MaterialLog::getMaterialType, materialType)
                        .eq(MaterialLog::getColor, color)
                        .orderByDesc(MaterialLog::getLogId)
                        .last("LIMIT 1")
        );
        return latest == null ? BigDecimal.ZERO : latest.getBalance();
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
        if (dto.getQualityScore() != null) {
            task.setQualityScore(dto.getQualityScore());
        }
        taskMapper.updateById(task);
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