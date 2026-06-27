package com.printclub.module.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志实体（对应 system_log 表）
 *
 * <p>记录所有"写"操作（任务审批/项目创建/打印机维护/耗材入库等）</p>
 *
 * @author F
 */
@Data
@TableName("system_log")
public class SystemLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    @TableField("user_id")
    private String userId;

    private String username;

    private String operation;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private String targetId;

    private String description;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("create_time")
    private LocalDateTime createTime;
}
