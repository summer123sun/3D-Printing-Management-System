package com.printclub.module.project.dto;

import lombok.Data;

/**
 * 项目查询参数
 *
 * @author E
 */
@Data
public class ProjectQuery {

    private Long page = 1L;
    private Long size = 10L;

    /** 项目状态 */
    private Integer status;

    /** 项目类型 */
    private Integer projectType;

    /** 我参与的：传 "true" 时过滤当前用户为成员 */
    private String scope;

    /** 关键字 */
    private String keyword;
}