package com.chaincat.pay.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 查询支付结果
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryPayResp {

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 支付方式上的交易ID
     */
    private String payMethodTransactionId;

    /**
     * 入口
     */
    private String entrance;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 用户ID
     */
    private String userId;

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
     * 支付状态：0-未支付，1-已支付，2-已关闭
     */
    private Integer status;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 业务方
     */
    private String biz;

    /**
     * 业务数据ID
     */
    private String bizDataId;

    /**
     * 业务附加数据
     */
    private String bizAttach;
}
