package com.printclub.module.log.service;

import com.printclub.common.result.PageResult;
import com.printclub.module.log.dto.LogQuery;
import com.printclub.module.log.entity.SystemLog;

/**
 * 系统日志 Service（M6 - 审计：仅 ADMIN 可见）
 *
 * @author F
 */
public interface LogService {

    /** 日志列表（分页 + 多维筛选） */
    PageResult<SystemLog> list(LogQuery query);

    /** 写日志（内部调用，service 层用 AOP 或手动调用） */
    void record(String userId, String username, String operation, String targetType, String targetId, String description, String ipAddress);

    /** 清理过期日志（保留 90 天） */
    int cleanOldLogs(int keepDays);
}
