package com.printclub.module.printer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维护记录实体（对应 maintenance 表）
 *
 * @author F
 */
@Data
@TableName("maintenance")
public class Maintenance implements Serializable {

    @TableId(value = "maint_id", type = IdType.AUTO)
    private Integer maintId;

    @TableField("printer_id")
    private String printerId;

    /** 1保养 2维修 3换件 4校准 */
    @TableField("maint_type")
    private Integer maintType;

    private String content;

    @TableField("maintainer_id")
    private String maintainerId;

    @TableField("maint_time")
    private LocalDateTime maintTime;

    @TableField("next_maint_date")
    private LocalDate nextMaintDate;

    @TableField("create_time")
    private LocalDateTime createTime;
}
