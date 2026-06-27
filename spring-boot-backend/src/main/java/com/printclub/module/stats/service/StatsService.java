package com.printclub.module.stats.service;

import java.util.Map;

/**
 * 统计 Service（M6 - 聚合查询，无写操作）
 *
 * @author F
 */
public interface StatsService {

    /** 总览 Dashboard：4 个核心数字 + 7 天趋势 */
    Object dashboard();

    /** 任务统计：按月/状态/材料 */
    Object taskStats(String startDate, String endDate);

    /** 材料消耗统计 */
    Object materialStats();

    /** 打印机使用率 */
    Object printerStats();

    /** 活跃社员排行 */
    Object memberRanking(int limit);
}
