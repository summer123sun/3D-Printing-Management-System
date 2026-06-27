package com.printclub.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打印任务实体（对应 print_task 表）
 *
 * <p>状态：0待审批 1已通过 2已驳回 3排队中 4打印中 5已完成 6已取消</p>
 * <p>优先级：1紧急 2普通 3低优</p>
 *
 * @author E（前端 B 也是你）
 */
@Data
@TableName("print_task")
public class PrintTask implements Serializable {

    @TableId(value = "task_id", type = IdType.INPUT)
    private String taskId;

    @TableField("applicant_id")
    private String applicantId;

    @TableField("approver_id")
    private String approverId;

    @TableField("printer_id")
    private String printerId;

    // ============== 关联字段（不映射到数据库） ==============

    /** 申请人姓名（v2：list 时从 member 表查） */
    @TableField(exist = false)
    private String applicantName;

    /** 审批人姓名（v2：list 时从 member 表查） */
    @TableField(exist = false)
    private String approverName;

    /** 打印机型号（v2：list 时从 printer 表查） */
    @TableField(exist = false)
    private String printerModel;

    @TableField("project_id")
    private Integer projectId;

    /** 任务标题 */
    private String title;

    @TableField("model_name")
    private String modelName;

    @TableField("stl_file_path")
    private String stlFilePath;

    @TableField("material_type")
    private String materialType;

    private String color;

    @TableField("layer_height")
    private BigDecimal layerHeight;

    @TableField("infill_rate")
    private Integer infillRate;

    /** 是否需要支撑：0否 1是 */
    @TableField("need_support")
    private Integer needSupport;

    /** 优先级：1紧急 2普通 3低优 */
    private Integer priority;

    @TableField("est_weight")
    private BigDecimal estWeight;

    @TableField("est_time")
    private Integer estTime;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("approve_comment")
    private String approveComment;

    /** 状态：0待审批 1已通过 2已驳回 3排队中 4打印中 5已完成 6已取消 */
    private Integer status;

    @TableField("actual_weight")
    private BigDecimal actualWeight;

    @TableField("actual_time")
    private Integer actualTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    /** 质量评分：1-5星 */
    @TableField("quality_score")
    private Integer qualityScore;

    /** 取件时间（NULL=未取） */
    @TableField("pickup_time")
    private LocalDateTime pickupTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // ========== 任务状态常量（Service 层判断用） ==========

    public static final int STATUS_PENDING    = 0;
    public static final int STATUS_APPROVED   = 1;
    public static final int STATUS_REJECTED   = 2;
    public static final int STATUS_QUEUED     = 3;
    public static final int STATUS_PRINTING   = 4;
    public static final int STATUS_DONE       = 5;
    public static final int STATUS_CANCELLED  = 6;
    public static final int STATUS_PICKED_UP  = 8;  // 已取件（v2：pickup() 改 status）

    public static final int PRIORITY_URGENT = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_LOW    = 3;

    public static final int NEED_SUPPORT_YES = 1;
    public static final int NEED_SUPPORT_NO  = 0;
}