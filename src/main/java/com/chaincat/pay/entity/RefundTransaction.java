package com.chaincat.pay.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款交易
 *
 * @author chenhaizhuang
 */
@Data
public class RefundTransaction {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 支付交易ID
     */
    private String payTransactionId;

    /**
     * 支付方式上的交易ID
     */
    private String payMethodTransactionId;

    /**
     * 退款金额
     */
    private BigDecimal amount;

    /**
     * 退款描述
     */
    private String description;

    /**
     * 退款状态：0-退款中，1-退款成功，2-退款失败
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
     * 支付交易
     */
    @TableField(exist = false)
    private PayTransaction payTransaction;
}
