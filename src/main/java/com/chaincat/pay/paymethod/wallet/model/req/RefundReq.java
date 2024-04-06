package com.chaincat.pay.paymethod.wallet.model.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款请求
 *
 * @author chenhaizhuang
 */
@Data
public class RefundReq {

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
