package com.chaincat.pay.feign.bootuser.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 钱包预支付结果
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
