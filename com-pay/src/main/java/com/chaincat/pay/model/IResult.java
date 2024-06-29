package com.chaincat.pay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口结果
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IResult<T> {

    /**
     * 成功码
     */
    public static final Integer SUCCESS_CODE = 0;

    /**
     * 成功信息
     */
    public static final String SUCCESS_MSG = "OK";

    /**
     * 失败码
     */
    public static final Integer FAIL_CODE = -1;

    /**
     * 失败信息
     */
    public static final String FAIL_MSG = "FAIL";

    /**
     * 错误码
     */
    public static final Integer ERROR_CODE = -100;

    /**
     * 错误信息
     */
    public static final String ERROR_MSG = "ERROR";

    /**
     * 结果码
     */
    private Integer code;

    /**
     * 结果信息
     */
    private String msg;

    /**
     * 接口数据
     */
    private T data;

    /**
     * 成功响应
     *
     * @param data 接口数据
     * @return IResult
     */
    public static <T> IResult<T> success(T data) {
        return IResult.<T>builder()
                .code(SUCCESS_CODE)
                .msg(SUCCESS_MSG)
                .data(data)
                .build();
    }

    /**
     * 成功响应
     *
     * @return IResult
     */
    public static IResult<Void> success() {
        return IResult.<Void>builder()
                .code(SUCCESS_CODE)
                .msg(SUCCESS_MSG)
                .build();
    }

    /**
     * 失败响应
     *
     * @param msg 结果信息
     * @return IResult
     */
    public static IResult<Void> fail(String msg) {
        return IResult.<Void>builder()
                .code(FAIL_CODE)
                .msg(FAIL_MSG)
                .build();
    }

    /**
     * 错误响应
     *
     * @return 接口结果
     */
    public static IResult<Void> error() {
        return IResult.<Void>builder()
                .code(ERROR_CODE)
                .msg(ERROR_MSG)
                .build();
    }
}
