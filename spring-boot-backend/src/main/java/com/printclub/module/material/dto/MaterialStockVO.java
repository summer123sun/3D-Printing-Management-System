package com.printclub.module.material.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 耗材库存视图（按 material_type + color 聚合）
 */
@Data
public class MaterialStockVO {

    private String materialType;

    private String color;

    /** 当前库存（克）—— 取该组合最近一次 balance */
    private BigDecimal currentStock;

    /** 最近一次入库时间 */
    private String lastUpdateTime;
}
