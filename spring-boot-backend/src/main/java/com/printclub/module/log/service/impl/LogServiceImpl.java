package com.printclub.module.log.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.result.PageResult;
import com.printclub.common.util.PageUtils;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.log.dto.LogQuery;
import com.printclub.module.log.entity.SystemLog;
import com.printclub.module.log.mapper.SystemLogMapper;
import com.printclub.module.log.service.LogService;
import com.printclub.module.user.entity.Member;
import com.printclub.module.user.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统日志 Service 实现
 *
 * @author F
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final SystemLogMapper systemLogMapper;
    // ✅ v2.2 修复（用户反馈）：日志搜索要支持姓名（之前只有学号）
    //    同时 recordCurrent 需要查真实姓名（之前 username 字段存的是学号）
    private final MemberMapper memberMapper;

    // 注入自身代理（@Lazy 解决循环依赖），用于 recordCurrent 内部调 record() 走 @Async 代理
    // 否则 self-invocation 会绕过 Spring AOP 代理，@Async 失效
    @Autowired
    @Lazy
    private LogService self;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<SystemLog> list(LogQuery query) {
        Page<SystemLog> p = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        // ✅ v2.2 修复：userId 字段同时匹配学号和姓名（LIKE OR）
        //    之前：eq(SystemLog::getUserId, query.getUserId()) → 只能精确匹配学号
        //    现在：like(userId) OR like(username) → 输入"刘洋"也能查到
        if (StrUtil.isNotBlank(query.getUserId())) {
            wrapper.and(w -> w
                    .like(SystemLog::getUserId, query.getUserId())
                    .or()
                    .like(SystemLog::getUsername, query.getUserId())
            );
        }
        if (StrUtil.isNotBlank(query.getOperation())) wrapper.like(SystemLog::getOperation, query.getOperation());
        if (StrUtil.isNotBlank(query.getTargetType())) wrapper.eq(SystemLog::getTargetType, query.getTargetType());
        if (StrUtil.isNotBlank(query.getTargetId())) wrapper.eq(SystemLog::getTargetId, query.getTargetId());
        if (StrUtil.isNotBlank(query.getStartTime())) {
            try {
                wrapper.ge(SystemLog::getCreateTime, LocalDateTime.parse(query.getStartTime(), FMT));
            } catch (Exception e) {
                log.warn("startTime 解析失败：{}", query.getStartTime());
            }
        }
        if (StrUtil.isNotBlank(query.getEndTime())) {
            try {
                wrapper.le(SystemLog::getCreateTime, LocalDateTime.parse(query.getEndTime(), FMT));
            } catch (Exception e) {
                log.warn("endTime 解析失败：{}", query.getEndTime());
            }
        }
        wrapper.orderByDesc(SystemLog::getLogId);
        return PageUtils.toResult(systemLogMapper.selectPage(p, wrapper));
    }

    @Override
    @Async
    public void record(String userId, String username, String operation, String targetType, String targetId, String description, String ipAddress) {
        try {
            SystemLog sysLog = new SystemLog();
            sysLog.setUserId(userId);
            sysLog.setUsername(username);
            sysLog.setOperation(operation);
            sysLog.setTargetType(targetType);
            sysLog.setTargetId(targetId);
            sysLog.setDescription(description);
            sysLog.setIpAddress(ipAddress);
            systemLogMapper.insert(sysLog);
        } catch (Exception e) {
            LogServiceImpl.log.error("记录系统日志失败：user={}, op={}", userId, operation, e);
        }
    }

    @Override
    public void recordCurrent(String operation, String targetType, String targetId, String description) {
        try {
            String userId = SecurityContext.getCurrentUserId();
            // ✅ v2.2 修复（用户反馈）：username 字段存真实姓名（之前存学号导致无法按姓名搜索）
            //    查 member 表拿 name，查不到（用户已注销）就 fallback 用 userId
            String username = userId;
            if (StrUtil.isNotBlank(userId)) {
                try {
                    Member member = memberMapper.selectById(userId);
                    if (member != null && StrUtil.isNotBlank(member.getName())) {
                        username = member.getName();
                    }
                } catch (Exception e) {
                    log.warn("recordCurrent 查 member 失败：userId={}", userId, e);
                }
            }
            // 尝试从当前 HTTP 请求取 IP（如果在线程里有的话）
            String ip = "internal";
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    String header = attrs.getRequest().getHeader("X-Forwarded-For");
                    if (StrUtil.isNotBlank(header)) {
                        ip = header.split(",")[0].trim();
                    } else {
                        ip = attrs.getRequest().getRemoteAddr();
                    }
                }
            } catch (Exception ignore) {
                // 非 Web 线程（定时任务 / 启动加载等）就用 "internal"
            }
            // 调 self.record() 走 Spring AOP 代理，@Async 才生效
            // （如果直接 this.record() 是 self-invocation，绕过代理，@Async 失效）
            self.record(userId, username, operation, targetType, targetId, description, ip);
        } catch (Exception e) {
            log.error("recordCurrent 失败：op={}", operation, e);
        }
    }

    @Override
    public int cleanOldLogs(int keepDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(keepDays);
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SystemLog::getCreateTime, threshold);
        int n = systemLogMapper.delete(wrapper);
        log.info("清理过期日志：{} 条（保留 {} 天）", n, keepDays);
        return n;
    }
}
