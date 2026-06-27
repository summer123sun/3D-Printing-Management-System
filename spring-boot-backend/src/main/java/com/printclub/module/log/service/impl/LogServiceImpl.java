package com.printclub.module.log.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.result.PageResult;
import com.printclub.common.util.PageUtils;
import com.printclub.module.log.dto.LogQuery;
import com.printclub.module.log.entity.SystemLog;
import com.printclub.module.log.mapper.SystemLogMapper;
import com.printclub.module.log.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<SystemLog> list(LogQuery query) {
        Page<SystemLog> p = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getUserId())) wrapper.eq(SystemLog::getUserId, query.getUserId());
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
            SystemLog log = new SystemLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setOperation(operation);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setDescription(description);
            log.setIpAddress(ipAddress);
            systemLogMapper.insert(log);
        } catch (Exception e) {
            LogServiceImpl.log.error("记录系统日志失败：user={}, op={}", userId, operation, e);
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
