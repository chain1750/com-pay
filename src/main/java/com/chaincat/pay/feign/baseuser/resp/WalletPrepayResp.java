package com.chaincat.pay.feign.baseuser.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包预支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class WalletPrepayResp {

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
