package com.chaincat.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款状态枚举
 *
 * @author chenhaizhuang
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum {

    IN_REFUND(0, "退款中"),
    REFUND_SUCCESS(1, "退款成功"),
    REFUND_FAIL(2, "退款失败");

    private final Integer value;

    private final String desc;

    public boolean valueEquals(Integer value) {
        return getValue().equals(value);
    }
}
