package com.chaincat.pay.model.req;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class PrepayReq {

    /**
     * 入口
     */
    @NotBlank(message = "入口不能为空")
    private String entrance;

    /**
     * 用户IP地址
     */
    @NotBlank(message = "用户IP地址不能为空")
    private String userIp;

    /**
     * 用户ID，项目中标识一个用户的唯一键
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 支付金额，单位元，两位小数
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "至少支付0.01元")
    private BigDecimal amount;

    /**
     * 支付描述，请简略描述，若需详细描述请由业务方存储
     */
    @NotBlank(message = "支付描述不能为空")
    private String description;

    /**
     * 过期时间，精确到毫秒
     */
    @NotNull(message = "过期时间不能为空")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    private LocalDateTime expireTime;

    /**
     * 业务方，业务方+业务数据ID唯一标识一次支付交易
     */
    @NotBlank(message = "业务方不能为空")
    private String biz;

    /**
     * 业务数据ID，对应业务方的唯一键，业务方+业务数据ID唯一标识一次支付交易
     */
    @NotBlank(message = "业务数据ID不能为空")
    private String bizDataId;

    /**
     * 业务附加数据，支付组件通知业务方或业务方查询支付时，原样返回该字段
     */
    private String bizAttach;

    /**
     * 业务通知MQ Topic
     */
    @NotBlank(message = "业务通知MQ Topic不能为空")
    private String bizMqTopic;
}
