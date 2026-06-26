package com.printclub.common.result;

import lombok.Data;

/**
 * 统一返回格式
 * <pre>
 * {
 *   "code": 200,
 *   "msg":  "操作成功",
 *   "data": {...}
 * }
 * </pre>
 *
 * @param <T> 数据类型
 * @author D
 */
@Data
public class Result<T> {

    /** 状态码：200 成功；4xx 业务错误；401 未登录；403 无权限；500 系统错误 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    public Result() {}

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ============== 静态工厂方法 ==============

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(ResultCode rc) {
        return new Result<>(rc.getCode(), rc.getMsg(), null);
    }

    public static <T> Result<T> error(ResultCode rc, String msg) {
        return new Result<>(rc.getCode(), msg, null);
    }
}