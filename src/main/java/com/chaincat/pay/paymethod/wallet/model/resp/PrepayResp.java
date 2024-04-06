package com.chaincat.pay.paymethod.wallet.model.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class PrepayResp {

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 签名
     */
    private String signature;
}
