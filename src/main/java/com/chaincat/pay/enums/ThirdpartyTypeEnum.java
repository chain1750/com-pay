package com.chaincat.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 第三方类型枚举
 *
 * @author chenhaizhuang
 */
@Getter
@AllArgsConstructor
public enum ThirdpartyTypeEnum {

    WECHAT(1, "微信"),
    ALIPAY(2, "支付宝");

    private final Integer value;

    private final String desc;

    public boolean valueEquals(Integer value) {
        return getValue().equals(value);
    }
}
