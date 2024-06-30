package com.chaincat.pay.feign.bootuser.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 钱包支付结果
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletPayResp {

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
