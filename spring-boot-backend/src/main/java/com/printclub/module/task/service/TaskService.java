package com.printclub.module.task.service;

import com.printclub.common.result.PageResult;
import com.printclub.module.task.dto.*;
import com.printclub.module.task.entity.PrintTask;

/**
 * 任务 Service（E - 核心业务）
 *
 * @author E
 */
public interface TaskService {

    /**
     * 提交打印任务（普通社员/新成员）
     *
     * @return 任务编号
     */
    String apply(TaskApplyDTO dto);

    /**
     * 我的任务列表（分页）
     */
    PageResult<PrintTask> myTasks(TaskQuery query);

    /**
     * 待审批任务列表（技术骨干用）
     */
    PageResult<PrintTask> pendingTasks(TaskQuery query);

    /**
     * 任务详情
     */
    PrintTask detail(String taskId);

    /**
     * 审批通过（技术骨干）
     */
    void approve(String taskId, ApproveDTO dto);

    /**
     * 审批驳回（技术骨干）
     */
    void reject(String taskId, RejectDTO dto);

    /**
     * 分配打印机（技术骨干）
     */
    void assignPrinter(String taskId, AssignPrinterDTO dto);

    /**
     * 开始打印（技术骨干）
     */
    void startPrint(String taskId);

    /**
     * 完成打印（技术骨干）
     *
     * <p>事务内：更新任务状态 + 扣减耗材库存 + 累计打印次数 + 自动归档作品库</p>
     */
    void finishPrint(String taskId, FinishPrintDTO dto);

    /**
     * 取件签到（任务申请人）
     */
    void pickup(String taskId, PickupDTO dto);

    /**
     * 取消任务（仅申请人本人可操作、且仅待审批/已通过状态可取消）
     */
    void cancel(String taskId);

    /**
     * 打印队列（按 priority → apply_time 排序）
     */
    PageResult<PrintTask> queue(TaskQuery query);

    /**
     * 任务统计（看板用）
     */
    Object stats();
}