package com.printclub.module.task.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.printclub.module.task.entity.PrintTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

/**
 * Task Mapper
 *
 * @author E
 */
@Mapper
public interface TaskMapper extends BaseMapper<PrintTask> {

    /**
     * v2 重构：返回当前被任务占用的打印机 ID 列表（QUEUED + PRINTING 状态）
     * 替代 PrinterServiceImpl.availablePrinters 和 TaskServiceImpl.assignPrinter
     * 两处复制粘贴的 selectObjs 查询逻辑
     */
    default List<String> selectBusyPrinterIds() {
        List<String> ids = selectObjs(
                new LambdaQueryWrapper<PrintTask>()
                        .in(PrintTask::getStatus, PrintTask.STATUS_QUEUED, PrintTask.STATUS_PRINTING)
                        .isNotNull(PrintTask::getPrinterId)
                        .select(PrintTask::getPrinterId)
        );
        return ids == null ? Collections.emptyList() : ids;
    }
}