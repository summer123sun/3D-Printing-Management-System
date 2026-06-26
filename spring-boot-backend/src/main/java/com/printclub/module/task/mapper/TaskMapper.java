package com.printclub.module.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.printclub.module.task.entity.PrintTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * Task Mapper
 *
 * @author E
 */
@Mapper
public interface TaskMapper extends BaseMapper<PrintTask> {
}