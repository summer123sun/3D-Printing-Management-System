package com.printclub.module.printer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.result.PageResult;
import com.printclub.module.printer.dto.MaintenanceCreateDTO;
import com.printclub.module.printer.entity.Maintenance;
import com.printclub.module.printer.entity.Printer;

import java.util.List;

/**
 * 打印机 Service（设备台账 + 维护记录）
 *
 * @author F
 */
public interface PrinterService {

    // ============ 打印机台账 ============

    /** 打印机列表 */
    PageResult<Printer> list(int page, int size, Integer status, String keyword);

    /** 打印机详情 */
    Printer detail(String printerId);

    /** 新增打印机（仅 ADMIN） */
    void create(Printer printer);

    /** 修改打印机（仅 ADMIN） */
    void update(Printer printer);

    /** 删除打印机（仅 ADMIN，限制：状态必须为报废） */
    void delete(String printerId);

    /** 设置打印机状态（仅 STAFF+） */
    void setStatus(String printerId, Integer status, String remark);

    /** 可用打印机列表（status=1） */
    List<Printer> availablePrinters();

    // ============ 维护记录 ============

    /** 维护记录（分页 + 按打印机筛选） */
    PageResult<Maintenance> maintenanceList(int page, int size, String printerId);

    /** 新增维护记录（仅 STAFF+） */
    void addMaintenance(MaintenanceCreateDTO dto, String currentUserId);

    /** 删除维护记录（仅 ADMIN） */
    void deleteMaintenance(Integer maintId);
}
