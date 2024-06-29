package com.chaincat.pay.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付交易
 *
 * @author chenhaizhuang
 */
@Data
public class PayTransaction {

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

    /**
     * 业务通知MQ Topic
     */
    private String bizMqTopic;
}
