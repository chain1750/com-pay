package com.chaincat.pay.feign.baseuser.req;

import lombok.Data;

/**
 * 钱包查询支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletQueryPayReq {

    /**
     * 外部交易ID
     */
    private String outTransactionId;
}
