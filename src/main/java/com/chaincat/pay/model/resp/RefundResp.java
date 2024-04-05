package com.chaincat.pay.model.resp;

import lombok.Data;

/**
 * 退款结果
 *
 * @author chenhaizhuang
 */
@Data
public class RefundResp {

    /**
     * 交易ID，交易的唯一标识
     */
    private String transactionId;
}
