package com.chaincat.pay.feign.baseuser.req;

import lombok.Data;

/**
 * 钱包关闭支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletClosePayReq {

    /**
     * 外部交易ID
     */
    private String outTransactionId;
}
