package com.printclub.module.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 耗材记录实体（对应 material_log 表）
 *
 * @author F
 */
@Data
@TableName("material_log")
public class MaterialLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Integer logId;

    @TableField("material_type")
    private String materialType;

    private String color;

    /** 重量变化（g，正数入库 负数消耗） */
    @TableField("weight_change")
    private BigDecimal weightChange;

    /** 变动后余额（g） */
    private BigDecimal balance;

    /** 操作类型：1入库 2消耗 */
    @TableField("operation_type")
    private Integer operationType;

    @TableField("related_task_id")
    private String relatedTaskId;

    @TableField("operator_id")
    private String operatorId;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    // ============== 关联字段（不映射到数据库） ==============

    /** 操作人姓名（v2：list 时从 member 表查） */
    @TableField(exist = false)
    private String operatorName;
}