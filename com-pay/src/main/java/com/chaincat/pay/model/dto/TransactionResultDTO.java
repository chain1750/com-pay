package com.chaincat.pay.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易结果
 *
 * @author chenhaizhuang
 */
@Data
public class TransactionResultDTO {

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 支付方式上的交易ID
     */
    private String payMethodTransactionId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 通知结果
     */
    @JSONField(serialize = false)
    private String notifyResult;
}
