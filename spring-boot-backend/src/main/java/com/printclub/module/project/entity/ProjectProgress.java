package com.printclub.module.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目阶段实体（对应 project_progress 表）
 *
 * @author E
 */
@Data
@TableName("project_progress")
public class ProjectProgress implements Serializable {

    @TableId(value = "progress_id", type = IdType.AUTO)
    private Integer progressId;

    @TableField("project_id")
    private Integer projectId;

    @TableField("stage_name")
    private String stageName;

    @TableField("stage_order")
    private Integer stageOrder;

    private String description;

    /** 0未开始 1进行中 2已完成 */
    private Integer status;

    @TableField("responsible_id")
    private String responsibleId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /** 阶段负责人姓名（来自 member 表，非数据库字段） */
    @TableField(exist = false)
    private String responsibleName;
}