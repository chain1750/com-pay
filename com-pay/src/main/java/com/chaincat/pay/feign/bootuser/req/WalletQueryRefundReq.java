package com.chaincat.pay.feign.bootuser.req;

import lombok.Data;

/**
 * 钱包查询退款请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletQueryRefundReq {

    /**
     * 外部交易ID
     */
    private String outTransactionId;
}
