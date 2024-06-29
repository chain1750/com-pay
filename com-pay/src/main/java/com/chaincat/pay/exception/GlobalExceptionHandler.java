package com.chaincat.pay.exception;

import com.chaincat.pay.model.IResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author chenhaizhuang
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验异常处理
     *
     * @param e 异常
     * @return Result
     */
    @ExceptionHandler(value = BindException.class)
    public IResult<Void> handleBindException(BindException e) {
        List<String> errors = e.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        String msg = String.join(",", errors);
        log.warn("参数校验异常：{}", msg);
        return IResult.fail(msg);
    }

    /**
     * 业务异常处理
     *
     * @param e 异常
     * @return Result
     */
    @ExceptionHandler(value = {CustomizeException.class, IllegalArgumentException.class, IllegalStateException.class})
    public IResult<Void> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常：{}", e.getMessage(), e);
        return IResult.fail(e.getMessage());
    }

    /**
     * 系统异常处理
     *
     * @param e 异常
     * @return Result
     */
    @ExceptionHandler(value = Exception.class)
    public IResult<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return IResult.error();
    }
}
