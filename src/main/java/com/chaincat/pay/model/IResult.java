package com.chaincat.pay.model;

import lombok.Data;

/**
 * 接口结果
 *
 * @author chenhaizhuang
 */
@Data
public class IResult<T> {

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
}
