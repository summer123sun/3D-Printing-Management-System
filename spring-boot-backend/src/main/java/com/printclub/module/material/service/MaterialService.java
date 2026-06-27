package com.printclub.module.material.service;

import com.printclub.common.result.PageResult;
import com.printclub.module.material.dto.MaterialInboundDTO;
import com.printclub.module.material.dto.MaterialStockVO;
import com.printclub.module.material.entity.MaterialLog;

import java.math.BigDecimal;
import java.util.List;

/**
 * 耗材 Service（F - 库存聚合 + 入库/消耗流水）
 *
 * @author F
 */
public interface MaterialService {

    /** 当前库存列表（按 material_type + color 聚合） */
    List<MaterialStockVO> stockList(String materialType);

    /** 库存预警（当前库存 < 500g 视为预警线，可调整） */
    List<MaterialStockVO> warningStocks(BigDecimal threshold);

    /** 耗材流水（分页 + 多维筛选） */
    PageResult<MaterialLog> logList(int page, int size, String materialType, String color, Integer operationType, String operatorId);

    /** 入库 */
    void inbound(MaterialInboundDTO dto, String currentUserId);

    /** 总览统计 */
    Object summary();
}
