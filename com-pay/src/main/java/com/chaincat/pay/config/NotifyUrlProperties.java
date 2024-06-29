package com.chaincat.pay.config;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 全局配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("pay.notify-url")
@RefreshScope
public class NotifyUrlProperties {

    /**
     * 支付地址
     */
    private String pay;

    /**
     * 退款地址
     */
    private String refund;

    public String getPayNotifyUrl(String entrance) {
        return StrUtil.format(pay, entrance);
    }

    public String getRefundNotifyUrl(String entrance) {
        return StrUtil.format(refund, entrance);
    }
}
