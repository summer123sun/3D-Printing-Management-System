package com.printclub.module.material.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.util.PageUtils;
import com.printclub.common.result.ResultCode;
import com.printclub.module.log.service.LogService;
import com.printclub.module.material.dto.MaterialInboundDTO;
import com.printclub.module.material.dto.MaterialStockVO;
import com.printclub.module.material.entity.MaterialLog;
import com.printclub.module.material.mapper.MaterialLogMapper;
import com.printclub.module.material.service.MaterialService;
import com.printclub.module.user.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 耗材 Service 实现
 *
 * @author F
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialLogMapper materialLogMapper;
    private final LogService logService;
    private final MemberMapper memberMapper;

    /** 默认预警线 500g */
    private static final BigDecimal DEFAULT_WARNING_THRESHOLD = new BigDecimal("500");

    @Override
    public List<MaterialStockVO> stockList(String materialType) {
        // 用 SQL 直接聚合：取每个 (material_type, color) 的最新 balance
        QueryWrapper<MaterialLog> wrapper = new QueryWrapper<>();
        wrapper.select("material_type", "color", "MAX(log_id) AS last_log_id");
        if (StrUtil.isNotBlank(materialType)) {
            wrapper.eq("material_type", materialType);
        }
        wrapper.groupBy("material_type", "color");
        wrapper.orderByAsc("material_type", "color");
        List<Map<String, Object>> latest = materialLogMapper.selectMaps(wrapper);

        // 再查这些 last_log_id 对应的 balance
        List<MaterialStockVO> result = new java.util.ArrayList<>();
        for (Map<String, Object> m : latest) {
            Integer lastId = ((Number) m.get("last_log_id")).intValue();
            MaterialLog log = materialLogMapper.selectById(lastId);
            if (log != null) {
                MaterialStockVO vo = new MaterialStockVO();
                vo.setMaterialType(log.getMaterialType());
                vo.setColor(log.getColor());
                vo.setCurrentStock(log.getBalance());
                vo.setLastUpdateTime(log.getCreateTime() == null ? null : log.getCreateTime().toString());
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<MaterialStockVO> warningStocks(BigDecimal threshold) {
        BigDecimal t = threshold == null ? DEFAULT_WARNING_THRESHOLD : threshold;
        List<MaterialStockVO> all = stockList(null);
        return all.stream()
                .filter(v -> v.getCurrentStock() != null && v.getCurrentStock().compareTo(t) < 0)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public PageResult<MaterialLog> logList(int page, int size, String materialType, String color, Integer operationType, String operatorId) {
        Page<MaterialLog> p = new Page<>(page, size);
        LambdaQueryWrapper<MaterialLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(materialType)) wrapper.eq(MaterialLog::getMaterialType, materialType);
        if (StrUtil.isNotBlank(color)) wrapper.eq(MaterialLog::getColor, color);
        if (operationType != null) wrapper.eq(MaterialLog::getOperationType, operationType);
        if (StrUtil.isNotBlank(operatorId)) wrapper.eq(MaterialLog::getOperatorId, operatorId);
        wrapper.orderByDesc(MaterialLog::getLogId);
        PageResult<MaterialLog> result = PageUtils.toResult(materialLogMapper.selectPage(p, wrapper));
        // v2：批量注入操作人姓名
        fillOperatorNames(result.getList());
        return result;
    }

    /**
     * v2：批量把 MaterialLog.operatorId 翻译成 operatorName（前端展示用）
     */
    private void fillOperatorNames(List<MaterialLog> logs) {
        if (logs == null || logs.isEmpty()) return;
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (MaterialLog l : logs) {
            if (l.getOperatorId() != null) ids.add(l.getOperatorId());
        }
        if (ids.isEmpty()) return;
        java.util.Map<String, String> id2name = new java.util.HashMap<>();
        for (com.printclub.module.user.entity.Member m : memberMapper.selectBatchIds(ids)) {
            id2name.put(m.getStudentId(), m.getName());
        }
        for (MaterialLog l : logs) {
            l.setOperatorName(id2name.get(l.getOperatorId()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(MaterialInboundDTO dto, String currentUserId) {
        if (dto.getWeightChange() == null || dto.getWeightChange().signum() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "入库重量必须为正数");
        }
        // 查当前库存（最新 balance）
        MaterialStockVO current = stockList(dto.getMaterialType()).stream()
                .filter(v -> dto.getColor().equals(v.getColor()))
                .findFirst().orElse(null);
        BigDecimal newBalance = current == null || current.getCurrentStock() == null
                ? dto.getWeightChange()
                : current.getCurrentStock().add(dto.getWeightChange());

        MaterialLog logEntry = new MaterialLog();
        logEntry.setMaterialType(dto.getMaterialType());
        logEntry.setColor(dto.getColor());
        logEntry.setWeightChange(dto.getWeightChange());
        logEntry.setBalance(newBalance);
        logEntry.setOperationType(1);  // 1入库
        logEntry.setOperatorId(currentUserId);
        logEntry.setRemark(dto.getRemark());
        materialLogMapper.insert(logEntry);
        log.info("耗材入库：{} {} +{}g, 余额={}g", dto.getMaterialType(), dto.getColor(), dto.getWeightChange(), newBalance);
        // 审计日志（v2 修复：之前 LogService 完全没接入）
        logService.recordCurrent("material.inbound", "material",
                dto.getMaterialType() + "/" + dto.getColor(),
                "耗材入库 +" + dto.getWeightChange() + "g，余额 " + newBalance + "g");
    }

    @Override
    public Object summary() {
        List<MaterialStockVO> stocks = stockList(null);
        BigDecimal total = BigDecimal.ZERO;
        int typeCount = 0;
        Map<String, BigDecimal> byType = new HashMap<>();
        for (MaterialStockVO v : stocks) {
            if (v.getCurrentStock() != null) {
                total = total.add(v.getCurrentStock());
                byType.merge(v.getMaterialType(), v.getCurrentStock(), BigDecimal::add);
            }
        }
        typeCount = byType.size();
        List<MaterialStockVO> warning = warningStocks(null);
        Map<String, Object> result = new HashMap<>();
        result.put("totalStock", total);
        result.put("typeCount", typeCount);
        result.put("warningCount", warning.size());
        result.put("byType", byType);
        return result;
    }
}
