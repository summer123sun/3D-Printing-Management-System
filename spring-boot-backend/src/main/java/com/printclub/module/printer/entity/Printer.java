package com.printclub.module.printer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打印机实体（对应 printer 表）
 *
 * @author F
 */
@Data
@TableName("printer")
public class Printer implements Serializable {

    @TableId(value = "printer_id", type = IdType.INPUT)
    private String printerId;

    private String model;

    private String brand;

    @TableField("purchase_date")
    private LocalDate purchaseDate;

    /** 状态：1正常 2维修中 3报废 */
    private Integer status;

    @TableField("total_print_hours")
    private BigDecimal totalPrintHours;

    private String location;

    @TableField("nozzle_size")
    private BigDecimal nozzleSize;

    @TableField("build_volume")
    private String buildVolume;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}