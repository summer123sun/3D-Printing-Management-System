package com.printclub.module.task.dto;

import lombok.Data;

/**
 * 任务查询参数
 *
 * @author E
 */
@Data
public class TaskQuery {

    /** 页码（从 1 开始） */
    private Long page = 1L;

    /** 每页大小 */
    private Long size = 10L;

    /** 任务状态（多选用逗号分隔字符串） */
    private String status;

    /** 打印机编号 */
    private String printerId;

    /** 申请人学号（管理员查询时可指定） */
    private String applicantId;

    /** 关键字（标题/模型名） */
    private String keyword;
}