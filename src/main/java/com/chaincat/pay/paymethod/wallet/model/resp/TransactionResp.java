package com.chaincat.pay.paymethod.wallet.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class TransactionResp {

    /**
     * 外部交易ID
     */
    private String outTransactionId;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}
