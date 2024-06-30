package com.chaincat.pay.feign.bootuser.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 钱包退款请求
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRefundReq {

    /**
     * 对应支付的交易ID
     */
    private String payOutTransactionId;

    /**
     * 外部交易ID
     */
    private String outTransactionId;

    /**
     * 退款金额
     */
    private BigDecimal amount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 通知地址
     */
    private String notifyUrl;
}
