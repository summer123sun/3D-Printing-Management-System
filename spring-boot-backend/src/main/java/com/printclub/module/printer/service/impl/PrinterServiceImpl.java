package com.printclub.module.printer.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.util.PageUtils;
import com.printclub.common.result.ResultCode;
import com.printclub.module.printer.dto.MaintenanceCreateDTO;
import com.printclub.module.printer.entity.Maintenance;
import com.printclub.module.printer.entity.Printer;
import com.printclub.module.printer.mapper.MaintenanceMapper;
import com.printclub.module.printer.mapper.PrinterMapper;
import com.printclub.module.printer.service.PrinterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 打印机 Service 实现
 *
 * @author F
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrinterServiceImpl implements PrinterService {

    private final PrinterMapper printerMapper;
    private final MaintenanceMapper maintenanceMapper;

    // ============== 状态常量 ==============
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_MAINTENANCE = 2;
    public static final int STATUS_SCRAPPED = 3;

    // ============== 打印机台账 ==============

    @Override
    public PageResult<Printer> list(int page, int size, Integer status, String keyword) {
        Page<Printer> p = new Page<>(page, size);
        LambdaQueryWrapper<Printer> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Printer::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Printer::getPrinterId, keyword)
                    .or().like(Printer::getModel, keyword)
                    .or().like(Printer::getBrand, keyword));
        }
        wrapper.orderByAsc(Printer::getPrinterId);
        return PageUtils.toResult(printerMapper.selectPage(p, wrapper));
    }

    @Override
    public Printer detail(String printerId) {
        Printer printer = printerMapper.selectById(printerId);
        if (printer == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "打印机不存在");
        }
        return printer;
    }

    @Override
    public void create(Printer printer) {
        if (printerMapper.selectById(printer.getPrinterId()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "打印机编号已存在");
        }
        if (printer.getStatus() == null) printer.setStatus(STATUS_NORMAL);
        if (printer.getTotalPrintHours() == null) printer.setTotalPrintHours(java.math.BigDecimal.ZERO);
        printerMapper.insert(printer);
        log.info("新增打印机：{}", printer.getPrinterId());
    }

    @Override
    public void update(Printer printer) {
        Printer exist = printerMapper.selectById(printer.getPrinterId());
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "打印机不存在");
        }
        printerMapper.updateById(printer);
        log.info("更新打印机：{}", printer.getPrinterId());
    }

    @Override
    public void delete(String printerId) {
        Printer exist = printerMapper.selectById(printerId);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "打印机不存在");
        }
        if (exist.getStatus() != STATUS_SCRAPPED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能删除【报废】状态的打印机");
        }
        // 同步删除维护记录
        LambdaQueryWrapper<Maintenance> mw = new LambdaQueryWrapper<>();
        mw.eq(Maintenance::getPrinterId, printerId);
        maintenanceMapper.delete(mw);
        printerMapper.deleteById(printerId);
        log.info("删除打印机：{}", printerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setStatus(String printerId, Integer status, String remark) {
        Printer exist = printerMapper.selectById(printerId);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "打印机不存在");
        }
        if (status == null || status < 1 || status > 3) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态值必须 1=正常 2=维修中 3=报废");
        }
        exist.setStatus(status);
        if (StrUtil.isNotBlank(remark)) {
            exist.setRemark(remark);
        }
        printerMapper.updateById(exist);
        log.info("设置打印机状态：{} → {}", printerId, status);
    }

    @Override
    public List<Printer> availablePrinters() {
        LambdaQueryWrapper<Printer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Printer::getStatus, STATUS_NORMAL)
                .orderByAsc(Printer::getPrinterId);
        return printerMapper.selectList(wrapper);
    }

    // ============== 维护记录 ==============

    @Override
    public PageResult<Maintenance> maintenanceList(int page, int size, String printerId) {
        Page<Maintenance> p = new Page<Maintenance>(page, size);
        LambdaQueryWrapper<Maintenance> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(printerId)) {
            wrapper.eq(Maintenance::getPrinterId, printerId);
        }
        wrapper.orderByDesc(Maintenance::getMaintTime);
        return PageUtils.toResult(maintenanceMapper.selectPage(p, wrapper));
    }

    @Override
    public void addMaintenance(MaintenanceCreateDTO dto, String currentUserId) {
        // 校验打印机存在
        if (printerMapper.selectById(dto.getPrinterId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "打印机不存在");
        }
        Maintenance m = new Maintenance();
        m.setPrinterId(dto.getPrinterId());
        m.setMaintType(dto.getMaintType());
        m.setContent(dto.getContent());
        m.setMaintainerId(currentUserId);
        m.setMaintTime(dto.getMaintTime() == null ? LocalDateTime.now() : dto.getMaintTime());
        m.setNextMaintDate(dto.getNextMaintDate());
        maintenanceMapper.insert(m);

        // 维护完成后，若维护类型是维修，自动把状态切回正常
        if (dto.getMaintType() != null && dto.getMaintType() == 2) {
            Printer p = printerMapper.selectById(dto.getPrinterId());
            if (p != null && p.getStatus() == STATUS_MAINTENANCE) {
                p.setStatus(STATUS_NORMAL);
                printerMapper.updateById(p);
            }
        }
        log.info("新增维护记录：printerId={}, type={}, by={}", dto.getPrinterId(), dto.getMaintType(), currentUserId);
    }

    @Override
    public void deleteMaintenance(Integer maintId) {
        if (maintenanceMapper.deleteById(maintId) == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维护记录不存在");
        }
        log.info("删除维护记录：{}", maintId);
    }
}
