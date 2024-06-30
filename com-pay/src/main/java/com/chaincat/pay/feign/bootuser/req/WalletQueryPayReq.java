package com.chaincat.pay.feign.bootuser.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 钱包查询支付请求
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletQueryPayReq {

    /**
     * 外部交易ID
     */
    private String outTransactionId;
}
