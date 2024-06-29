package com.chaincat.pay.paymethod.alipay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.chaincat.pay.exception.CustomizeException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付宝支付工厂
 *
 * @author chenhaizhuang
 */
public class AlipayPayFactory {

    private static final Map<String, AlipayClient> ALIPAY_CLIENT = new ConcurrentHashMap<>();

    public static synchronized AlipayPayProperties.Account getAccount(AlipayPayProperties alipayPayProperties,
                                                                      String entrance) {
        String appId = alipayPayProperties.getEntrances().getOrDefault(entrance, "");
        Optional<AlipayPayProperties.Account> optionalAccount = alipayPayProperties.getAccounts().stream()
                .filter(e -> e.getApps().containsKey(appId))
                .findFirst();
        return optionalAccount.orElseThrow(() -> new CustomizeException("入口没有可用的支付方式"));
    }

    public static synchronized AlipayClient getAlipayClient(AlipayPayProperties alipayPayProperties,
                                                            String entrance) {
        String appId = alipayPayProperties.getEntrances().get(entrance);
        if (ALIPAY_CLIENT.containsKey(appId)) {
            return ALIPAY_CLIENT.get(appId);
        }
        AlipayPayProperties.Account account = getAccount(alipayPayProperties, entrance);

        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(alipayPayProperties.getServerUrl());
        alipayConfig.setAppId(appId);
        alipayConfig.setPrivateKey(account.getApps().get(appId));
        alipayConfig.setAlipayPublicKey(account.getPublicKey());
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            ALIPAY_CLIENT.put(appId, alipayClient);
            return alipayClient;
        } catch (Exception e) {
            throw new CustomizeException("创建支付宝客户端失败", e);
        }
    }
}
