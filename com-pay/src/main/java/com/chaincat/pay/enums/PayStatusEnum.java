package com.chaincat.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author chenhaizhuang
 */
@Getter
@AllArgsConstructor
public enum PayStatusEnum {

    NOT_PAY(0, "未支付"),
    PAY_SUCCESS(1, "已支付"),
    PAY_CLOSED(2, "已关闭");

    private final Integer value;

    private final String desc;

    public boolean valueEquals(Integer value) {
        return getValue().equals(value);
    }
}
