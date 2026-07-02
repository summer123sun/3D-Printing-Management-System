package com.printclub.common.exception;

import com.printclub.common.result.Result;
import com.printclub.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理
 * 把所有异常翻译成统一的 {@link Result} 返回
 *
 * @author D
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常 [{}]：{}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** @Valid 校验失败（@RequestBody 触发的） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", msg);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 表单绑定异常 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败：{}", msg);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.SERVER_ERROR));
    }

    /**
     * ✅ v2.13 修复（审查发现）：之前文件超过 max-file-size 直接 500，
     *    前端 catch 静默失败，用户反复提交都"没反应"
     *    修法：单独捕获 MaxUploadSizeExceededException，返回友好错误
     *    同时处理 FileSizeLimitExceededException（Tomcat 内部抛的）
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class,
                       org.apache.tomcat.util.http.fileupload.FileSizeLimitExceededException.class,
                       org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeException.class})
    public Result<Void> handleUploadSizeExceeded(Exception e) {
        log.warn("文件上传超过大小限制：{}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(),
                "文件过大，单个文件最大 50MB");
    }
}