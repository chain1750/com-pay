package com.chaincat.pay.paymethod.wallet.model.req;

import lombok.Data;

/**
 * 查询请求
 *
 * @author chenhaizhuang
 */
@Data
public class QueryReq {

    /**
     * 外部交易ID
     */
    private String outTransactionId;
}
