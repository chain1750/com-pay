package com.chaincat.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 入口配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("pay")
@RefreshScope
public class EntranceProperties {

    /**
     * 入口与支付方式映射
     */
    private Map<String, String> entrances;
}
