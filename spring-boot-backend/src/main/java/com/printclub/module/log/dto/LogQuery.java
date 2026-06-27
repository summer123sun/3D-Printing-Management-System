package com.printclub.module.log.dto;

import lombok.Data;

/**
 * 系统日志查询条件
 */
@Data
public class LogQuery {

    private Integer page = 1;
    private Integer size = 20;

    /** 按操作人学号 */
    private String userId;

    /** 按操作类型筛选（如 task.approve / printer.maintain） */
    private String operation;

    /** 按对象类型筛选（如 task / project / printer / material） */
    private String targetType;

    /** 按对象 ID */
    private String targetId;

    /** 起始时间（yyyy-MM-dd HH:mm:ss） */
    private String startTime;

    /** 结束时间 */
    private String endTime;
}
