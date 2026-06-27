package com.printclub.module.log.service;

import com.printclub.common.result.PageResult;
import com.printclub.module.log.dto.LogQuery;
import com.printclub.module.log.entity.SystemLog;

/**
 * 系统日志 Service（M6 - 审计：仅 ADMIN 可见）
 *
 * <p>v2 改进：所有 Service 在关键操作后只需调 {@link #recordCurrent}，
 * 自动从 SecurityContext 取当前用户 + 从 RequestContextHolder 取 IP，
 * 内部异步写入 system_log 表（@Async）。</p>
 *
 * <h3>TODO AOP 化（问题 7）</h3>
 * <p>当前 6 个 Service 手动调 recordCurrent() 共 ~25 次，后续可用 AOP 替代：</p>
 * <pre>
 *   // 1. 定义注解
 *   {@code @LogRecord(operation = "task.approve", targetType = "task", targetId = "#taskId")}
 *   public void approve(String taskId, ...) { ... }
 *
 *   // 2. 定义 Aspect
 *   {@code @Aspect @Component public class LogRecordAspect {
 *       @Around("@annotation(logRecord)")
 *       public Object around(ProceedingJoinPoint pjp, LogRecord logRecord) {
 *           Object result = pjp.proceed();
 *           logService.recordCurrent(logRecord.operation(), logRecord.targetType(),
 *               SpelUtil.parse(pjp, logRecord.targetId()),  // SpEL 取参数
 *               SpelUtil.parse(pjp, logRecord.description()));
 *           return result;
 *       }
 *   }}
 *
 *   // 3. 移除所有手动调用 logService.recordCurrent()
 * </pre>
 * <p>优点：</p>
 * <ul>
 *   <li>代码更干净（Service 不再关心日志）</li>
 *   <li>统一拦截（漏写的操作也能被自动记录）</li>
 *   <li>targetId / description 支持 SpEL 取方法参数</li>
 * </ul>
 *
 * @author F
 */
public interface LogService {

    /** 日志列表（分页 + 多维筛选） */
    PageResult<SystemLog> list(LogQuery query);

    /**
     * 写日志（全参数版，向后兼容）
     */
    void record(String userId, String username, String operation, String targetType, String targetId, String description, String ipAddress);

    /**
     * 写日志（自动从 SecurityContext 取当前用户和 IP，推荐使用）
     *
     * @param operation   操作类型，如 task.apply / task.approve / project.create
     * @param targetType  操作对象类型，如 task / project / material / member / artwork
     * @param targetId    操作对象 ID（可空）
     * @param description 操作描述（可空）
     */
    void recordCurrent(String operation, String targetType, String targetId, String description);

    /** 清理过期日志（保留 N 天） */
    int cleanOldLogs(int keepDays);
}
