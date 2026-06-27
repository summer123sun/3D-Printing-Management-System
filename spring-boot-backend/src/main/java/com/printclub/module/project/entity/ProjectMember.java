package com.printclub.module.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目成员实体（对应 project_member 表）
 *
 * <p>UNIQUE 约束 (project_id, member_id)</p>
 *
 * @author E
 */
@Data
@TableName("project_member")
public class ProjectMember implements Serializable {

    @TableId(value = "pm_id", type = IdType.AUTO)
    private Integer pmId;

    @TableField("project_id")
    private Integer projectId;

    @TableField("member_id")
    private String memberId;

    /** 项目内角色：1负责人 2核心成员 3参与成员 */
    @TableField("role_in_project")
    private Integer roleInProject;

    private String contribution;

    @TableField("join_time")
    private LocalDateTime joinTime;

    /** 状态：1进行中 2已退出 3已完成 */
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /** 真实姓名（来自 member 表，@TableField(exist=false) 表示非数据库字段） */
    @TableField(exist = false)
    private String memberName;
}