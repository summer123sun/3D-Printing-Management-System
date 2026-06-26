package com.printclub.common.result;

import lombok.Getter;

/**
 * 业务错误码
 *
 * <ul>
 *   <li>2xx：成功</li>
 *   <li>4xx：业务错误（参数、权限、资源不存在等）</li>
 *   <li>5xx：系统错误</li>
 * </ul>
 *
 * @author D
 */
@Getter
public enum ResultCode {

    // ===== 通用 =====
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器错误"),

    // ===== 认证模块 10xx =====
    LOGIN_FAIL(1001, "学号或密码错误"),
    TOKEN_EXPIRED(1002, "登录已过期"),
    TOKEN_INVALID(1003, "无效 token"),

    // ===== 用户模块 11xx =====
    USER_NOT_FOUND(1101, "用户不存在"),

    // ===== 任务模块 12xx =====
    TASK_NOT_FOUND(1201, "任务不存在"),
    TASK_STATUS_INVALID(1202, "任务状态不允许此操作"),
    TASK_NO_PRINTER(1203, "未分配打印机"),
    TASK_INSUFFICIENT_STOCK(1204, "耗材库存不足"),
    TASK_ALREADY_PICKED_UP(1205, "任务已被签收"),
    TASK_NOT_YOURS(1206, "无权操作他人的任务"),

    // ===== 项目模块 13xx =====
    PROJECT_NOT_FOUND(1301, "项目不存在"),
    PROJECT_NOT_OWNER(1302, "仅项目负责人可操作"),
    PROJECT_MEMBER_EXISTS(1303, "成员已在项目中"),
    PROJECT_STATUS_INVALID(1304, "项目状态不允许此操作"),

    // ===== 作品库模块 14xx =====
    ARTWORK_NOT_FOUND(1401, "作品不存在"),

    // ===== 设备模块 15xx =====
    PRINTER_NOT_FOUND(1501, "打印机不存在"),
    PRINTER_BUSY(1502, "打印机正在打印任务"),
    PRINTER_BROKEN(1503, "打印机处于维修/报废状态"),

    // ===== 耗材模块 16xx =====
    MATERIAL_NOT_FOUND(1601, "耗材类型/颜色不存在"),
    ;

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}