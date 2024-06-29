package com.chaincat.pay.paymethod.wallet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 钱包支付配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("pay.methods.wallet")
@RefreshScope
public class WalletPayProperties {

    /**
     * 盐值
     */
    private String salt;

    /**
     * 签名字段
     */
    private String signParamKey;
}
