package com.printclub.module.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体（对应 project 表）
 *
 * <p>状态：0筹备中 1进行中 2已完成 3已取消</p>
 * <p>类型：1作品创作 2竞赛备赛 3定制订单 4社团活动</p>
 *
 * @author E
 */
@Data
@TableName("project")
public class Project implements Serializable {

    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;

    @TableField("project_name")
    private String projectName;

    /** 类型：1作品创作 2竞赛备赛 3定制订单 4社团活动 */
    @TableField("project_type")
    private Integer projectType;

    @TableField("leader_id")
    private String leaderId;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("actual_end_date")
    private LocalDate actualEndDate;

    private BigDecimal budget;

    @TableField("actual_cost")
    private BigDecimal actualCost;

    private String description;

    private String deliverables;

    @TableField("cover_image")
    private String coverImage;

    /** 状态：0筹备中 1进行中 2已完成 3已取消 */
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // ============== 关联字段（不映射到数据库） ==============

    /** 负责人姓名（v2：list 时从 member 表查，前端展示用） */
    @TableField(exist = false)
    private String leaderName;

    // ============== 常量 ==============

    public static final int STATUS_PREPARING = 0;
    public static final int STATUS_RUNNING   = 1;
    public static final int STATUS_DONE      = 2;
    public static final int STATUS_CANCELLED = 3;

    public static final int TYPE_CREATION = 1;
    public static final int TYPE_COMPETE  = 2;
    public static final int TYPE_ORDER    = 3;
    public static final int TYPE_ACTIVITY = 4;

    /** 项目内角色 */
    public static final int ROLE_LEADER     = 1;
    public static final int ROLE_CORE       = 2;
    public static final int ROLE_PARTICIPANT = 3;

    /** 项目成员状态 */
    public static final int MEMBER_STATUS_ACTIVE   = 1;
    public static final int MEMBER_STATUS_QUIT     = 2;
    public static final int MEMBER_STATUS_COMPLETE = 3;

    /** 阶段状态 */
    public static final int STAGE_STATUS_PENDING = 0;
    public static final int STAGE_STATUS_RUNNING  = 1;
    public static final int STAGE_STATUS_DONE     = 2;
}