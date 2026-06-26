package com.printclub.common.exception;

import com.printclub.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>Service 层抛出，由 GlobalExceptionHandler 统一处理</p>
 *
 * @author D
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(ResultCode rc) {
        super(rc.getMsg());
        this.code = rc.getCode();
    }

    public BusinessException(ResultCode rc, String msg) {
        super(msg);
        this.code = rc.getCode();
    }
}