package com.printclub.module.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.printclub.module.artwork.entity.Artwork;
import com.printclub.module.artwork.mapper.ArtworkMapper;
import com.printclub.module.material.entity.MaterialLog;
import com.printclub.module.material.mapper.MaterialLogMapper;
import com.printclub.module.printer.entity.Printer;
import com.printclub.module.printer.mapper.PrinterMapper;
import com.printclub.module.stats.service.StatsService;
import com.printclub.module.task.entity.PrintTask;
import com.printclub.module.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计 Service 实现
 *
 * @author F
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final TaskMapper taskMapper;
    private final ArtworkMapper artworkMapper;
    private final MaterialLogMapper materialLogMapper;
    private final PrinterMapper printerMapper;
    private final com.printclub.module.user.mapper.MemberMapper memberMapper;

    @Override
    public Object dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 任务核心数字
        long totalTasks = taskMapper.selectCount(null);
        long doneTasks = taskMapper.selectCount(new QueryWrapper<PrintTask>().eq("status", 5));
        long pendingTasks = taskMapper.selectCount(new QueryWrapper<PrintTask>().eq("status", 0));
        result.put("totalTasks", totalTasks);
        result.put("doneTasks", doneTasks);
        result.put("pendingTasks", pendingTasks);
        result.put("completionRate", totalTasks == 0 ? 0 : Math.round(doneTasks * 1000.0 / totalTasks) / 10.0);

        // 累计打印时长
        QueryWrapper<PrintTask> hoursWrapper = new QueryWrapper<>();
        hoursWrapper.select("IFNULL(SUM(actual_time), 0) AS total");
        Map<String, Object> hours = taskMapper.selectMaps(hoursWrapper).get(0);
        result.put("totalPrintHours", hours.get("total"));

        // 累计耗材
        QueryWrapper<PrintTask> weightWrapper = new QueryWrapper<>();
        weightWrapper.select("IFNULL(SUM(actual_weight), 0) AS total");
        Map<String, Object> weight = taskMapper.selectMaps(weightWrapper).get(0);
        result.put("totalWeight", weight.get("total"));

        // 作品数
        result.put("totalArtworks", artworkMapper.selectCount(null));
        result.put("recommendedArtworks", artworkMapper.selectCount(new QueryWrapper<Artwork>().eq("is_recommended", 1)));

        // 打印机
        result.put("totalPrinters", printerMapper.selectCount(null));
        result.put("normalPrinters", printerMapper.selectCount(new QueryWrapper<Printer>().eq("status", 1)));

        // 7 天趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            long count = taskMapper.selectCount(new QueryWrapper<PrintTask>()
                    .ge("apply_time", start).lt("apply_time", end));
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("date", day.toString());
            t.put("count", count);
            trend.add(t);
        }
        result.put("trend7d", trend);

        return result;
    }

    @Override
    public Object taskStats(String startDate, String endDate) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 按状态分布
        QueryWrapper<PrintTask> statusWrapper = new QueryWrapper<>();
        statusWrapper.select("status", "COUNT(*) AS count");
        if (startDate != null && !startDate.isEmpty()) {
            statusWrapper.ge("apply_time", startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            statusWrapper.le("apply_time", endDate + " 23:59:59");
        }
        statusWrapper.groupBy("status");
        result.put("byStatus", taskMapper.selectMaps(statusWrapper));

        // 按材料分布
        QueryWrapper<PrintTask> matWrapper = new QueryWrapper<>();
        matWrapper.select("material_type", "COUNT(*) AS count");
        if (startDate != null && !startDate.isEmpty()) matWrapper.ge("apply_time", startDate + " 00:00:00");
        if (endDate != null && !endDate.isEmpty()) matWrapper.le("apply_time", endDate + " 23:59:59");
        matWrapper.groupBy("material_type");
        result.put("byMaterial", taskMapper.selectMaps(matWrapper));

        // 按月趋势（最近 6 个月）
        List<Map<String, Object>> monthly = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            LocalDate m = LocalDate.now().minusMonths(i);
            String prefix = m.format(fmt);
            long count = taskMapper.selectCount(new QueryWrapper<PrintTask>()
                    .apply("DATE_FORMAT(apply_time, '%Y-%m') = {0}", prefix));
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("month", prefix);
            t.put("count", count);
            monthly.add(t);
        }
        result.put("monthly", monthly);

        return result;
    }

    @Override
    public Object materialStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 按材料类型消耗
        QueryWrapper<MaterialLog> wrapper = new QueryWrapper<>();
        wrapper.select("material_type", "SUM(ABS(weight_change)) AS total_used", "COUNT(*) AS log_count")
                .eq("operation_type", 2)
                .groupBy("material_type");
        result.put("byMaterial", materialLogMapper.selectMaps(wrapper));

        // 当前库存
        result.put("currentStock", materialLogMapper.selectList(null));  // 简化：返回所有流水

        return result;
    }

    @Override
    public Object printerStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 打印机列表 + 累计时长
        List<Printer> printers = printerMapper.selectList(null);
        result.put("printers", printers);

        // 总累计时长
        BigDecimal total = BigDecimal.ZERO;
        for (Printer p : printers) {
            if (p.getTotalPrintHours() != null) total = total.add(p.getTotalPrintHours());
        }
        result.put("totalHours", total);
        result.put("count", printers.size());
        result.put("normalCount", printers.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count());
        result.put("maintCount", printers.stream().filter(p -> p.getStatus() != null && p.getStatus() == 2).count());
        result.put("scrappedCount", printers.stream().filter(p -> p.getStatus() != null && p.getStatus() == 3).count());

        return result;
    }

    @Override
    public Object memberRanking(int limit) {
        // 按完成任务数排序
        QueryWrapper<PrintTask> wrapper = new QueryWrapper<>();
        wrapper.select("applicant_id", "COUNT(*) AS done_count", "SUM(actual_time) AS total_hours", "SUM(actual_weight) AS total_weight")
                .eq("status", 5)
                .groupBy("applicant_id")
                .orderByDesc("done_count")
                .last("LIMIT " + limit);
        List<Map<String, Object>> rows = taskMapper.selectMaps(wrapper);

        // v2：批量注入社员姓名（前端 Top 8 展示用，跟 AppHeader 显示姓名一致）
        if (rows != null && !rows.isEmpty()) {
            java.util.Set<String> ids = new java.util.HashSet<>();
            for (Map<String, Object> row : rows) {
                Object id = row.get("applicant_id");
                if (id != null) ids.add(id.toString());
            }
            java.util.Map<String, String> id2name = new java.util.HashMap<>();
            if (!ids.isEmpty()) {
                for (com.printclub.module.user.entity.Member m : memberMapper.selectBatchIds(ids)) {
                    id2name.put(m.getStudentId(), m.getName());
                }
            }
            for (Map<String, Object> row : rows) {
                Object id = row.get("applicant_id");
                row.put("applicant_name", id == null ? null : id2name.get(id.toString()));
            }
        }
        return rows;
    }
}
