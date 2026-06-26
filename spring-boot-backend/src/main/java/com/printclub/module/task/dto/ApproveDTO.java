package com.printclub.module.task.dto;

import lombok.Data;

/**
 * 审批通过请求
 *
 * @author E
 */
@Data
public class ApproveDTO {

    /** 审批意见（可选） */
    private String approveComment;
}