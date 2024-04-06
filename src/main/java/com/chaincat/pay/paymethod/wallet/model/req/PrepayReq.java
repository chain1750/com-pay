package com.chaincat.pay.paymethod.wallet.model.req;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class PrepayReq {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 外部交易ID
     */
    private String outTransactionId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 通知地址
     */
    private String notifyUrl;
}
