package com.printclub.module.material.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 耗材入库 DTO
 */
@Data
public class MaterialInboundDTO {

    @NotNull
    private String materialType;  // PLA/PETG/TPU/ABS

    @NotNull
    private String color;

    @NotNull
    @Positive
    private BigDecimal weightChange;  // 入库重量（克，正数）

    private String remark;
}
