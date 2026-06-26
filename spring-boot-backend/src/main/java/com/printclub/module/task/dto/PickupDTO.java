package com.printclub.module.task.dto;

import lombok.Data;

/**
 * 取件签到请求
 *
 * @author E
 */
@Data
public class PickupDTO {

    /** 申请人对打印质量的评价（1-5，可选） */
    private Integer qualityScore;
}