package com.chaincat.pay.utils;

import cn.hutool.core.lang.Assert;
import com.chaincat.pay.model.IResult;

/**
 * 接口结果工具
 *
 * @author chenhaizhuang
 */
public class IResultUtils {

    public static final Integer SUCCESS_CODE = 0;

    public static final Integer FAIL_CODE = -1;

    public static final Integer ERROR_CODE = -100;

    public static <T> IResult<T> success(T data) {
        IResult<T> result = new IResult<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("OK");
        result.setData(data);
        return result;
    }

    public static IResult<Void> success() {
        IResult<Void> result = new IResult<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("OK");
        return result;
    }

    public static IResult<Void> fail(String msg) {
        IResult<Void> result = new IResult<>();
        result.setCode(FAIL_CODE);
        result.setMsg(msg);
        return result;
    }

    public static IResult<Void> error() {
        IResult<Void> result = new IResult<>();
        result.setCode(ERROR_CODE);
        result.setMsg("ERROR");
        return result;
    }

    public static <T> boolean checkAndReturn(IResult<T> result) {
        return SUCCESS_CODE.equals(result.getCode());
    }

    public static <T> void checkAndThrow(IResult<T> result) {
        Assert.isTrue(SUCCESS_CODE.equals(result.getCode()), result.getMsg());
    }
}
