package com.chaincat.pay.paymethod.wechat.config;

import com.chaincat.pay.exception.CustomizeException;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.app.AppServiceExtension;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信支付工厂
 *
 * @author chenhaizhuang
 */
public class WeChatPayFactory {

    private static final Map<String, RSAAutoCertificateConfig> RSA_AUTO_CERTIFICATE_CONFIG = new ConcurrentHashMap<>();

    private static final Map<String, NotificationParser> NOTIFICATION_PARSERS = new ConcurrentHashMap<>();

    private static final Map<String, RefundService> REFUND_SERVICE = new ConcurrentHashMap<>();

    private static final Map<String, AppServiceExtension> APP_SERVICE_EXTENSION = new ConcurrentHashMap<>();

    private static final Map<String, H5Service> H5_SERVICE = new ConcurrentHashMap<>();

    private static final Map<String, JsapiServiceExtension> JSAPI_SERVICE_EXTENSION = new ConcurrentHashMap<>();

    private static final Map<String, NativePayService> NATIVE_PAY_SERVICE = new ConcurrentHashMap<>();

    public static synchronized WeChatPayProperties.Merchant getMerchant(WeChatPayProperties weChatPayProperties,
                                                                  String entrance) {
        String appId = weChatPayProperties.getEntrances().getOrDefault(entrance, "");
        Optional<WeChatPayProperties.Merchant> optionalMerchant = weChatPayProperties.getMerchants().stream()
                .filter(e -> e.getAppIds().contains(appId))
                .findFirst();
        if (optionalMerchant.isEmpty()) {
            throw new CustomizeException("入口没有可用的支付方式");
        }
        return optionalMerchant.get();
    }

    public static synchronized RSAAutoCertificateConfig getRsaAutoCertificateConfig(WeChatPayProperties weChatPayProperties,
                                                                              String entrance) {
        WeChatPayProperties.Merchant merchant = getMerchant(weChatPayProperties, entrance);
        if (RSA_AUTO_CERTIFICATE_CONFIG.containsKey(merchant.getMerchantId())) {
            return RSA_AUTO_CERTIFICATE_CONFIG.get(merchant.getMerchantId());
        }
        RSAAutoCertificateConfig rsaAutoCertificateConfig = new RSAAutoCertificateConfig.Builder()
                .merchantId(merchant.getMerchantId())
                .privateKeyFromPath(merchant.getPrivateKeyPath())
                .merchantSerialNumber(merchant.getSerialNumber())
                .apiV3Key(merchant.getApiV3Key())
                .build();
        RSA_AUTO_CERTIFICATE_CONFIG.put(merchant.getMerchantId(), rsaAutoCertificateConfig);
        return rsaAutoCertificateConfig;
    }

    public static synchronized NotificationParser getNotificationParser(WeChatPayProperties weChatPayProperties,
                                                                 String entrance) {
        WeChatPayProperties.Merchant merchant = getMerchant(weChatPayProperties, entrance);
        if (NOTIFICATION_PARSERS.containsKey(merchant.getMerchantId())) {
            return NOTIFICATION_PARSERS.get(merchant.getMerchantId());
        }
        RSAAutoCertificateConfig rsaAutoCertificateConfig = getRsaAutoCertificateConfig(weChatPayProperties, entrance);
        NotificationParser notificationParser = new NotificationParser(rsaAutoCertificateConfig);
        NOTIFICATION_PARSERS.put(merchant.getMerchantId(), notificationParser);
        return notificationParser;
    }

    public static synchronized RefundService getRefundService(WeChatPayProperties weChatPayProperties,
                                                       String entrance) {
        WeChatPayProperties.Merchant merchant = getMerchant(weChatPayProperties, entrance);
        if (REFUND_SERVICE.containsKey(merchant.getMerchantId())) {
            return REFUND_SERVICE.get(merchant.getMerchantId());
        }
        RSAAutoCertificateConfig rsaAutoCertificateConfig = getRsaAutoCertificateConfig(weChatPayProperties, entrance);
        RefundService refundService = new RefundService.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
        REFUND_SERVICE.put(merchant.getMerchantId(), refundService);
        return refundService;
    }

    public static synchronized AppServiceExtension getAppServiceExtension(WeChatPayProperties weChatPayProperties,
                                                                   String entrance) {
        WeChatPayProperties.Merchant merchant = getMerchant(weChatPayProperties, entrance);
        if (APP_SERVICE_EXTENSION.containsKey(merchant.getMerchantId())) {
            return APP_SERVICE_EXTENSION.get(merchant.getMerchantId());
        }
        RSAAutoCertificateConfig rsaAutoCertificateConfig = getRsaAutoCertificateConfig(weChatPayProperties, entrance);
        AppServiceExtension appServiceExtension = new AppServiceExtension.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
        APP_SERVICE_EXTENSION.put(merchant.getMerchantId(), appServiceExtension);
        return appServiceExtension;
    }

    public static synchronized H5Service getH5Service(WeChatPayProperties weChatPayProperties,
                                               String entrance) {
        WeChatPayProperties.Merchant merchant = getMerchant(weChatPayProperties, entrance);
        if (H5_SERVICE.containsKey(merchant.getMerchantId())) {
            return H5_SERVICE.get(merchant.getMerchantId());
        }
        RSAAutoCertificateConfig rsaAutoCertificateConfig = getRsaAutoCertificateConfig(weChatPayProperties, entrance);
        H5Service h5Service = new H5Service.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
        H5_SERVICE.put(merchant.getMerchantId(), h5Service);
        return h5Service;
    }

    public static synchronized JsapiServiceExtension getJsapiServiceExtension(WeChatPayProperties weChatPayProperties,
                                                                       String entrance) {
        WeChatPayProperties.Merchant merchant = getMerchant(weChatPayProperties, entrance);
        if (JSAPI_SERVICE_EXTENSION.containsKey(merchant.getMerchantId())) {
            return JSAPI_SERVICE_EXTENSION.get(merchant.getMerchantId());
        }
        RSAAutoCertificateConfig rsaAutoCertificateConfig = getRsaAutoCertificateConfig(weChatPayProperties, entrance);
        JsapiServiceExtension jsapiServiceExtension = new JsapiServiceExtension.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
        JSAPI_SERVICE_EXTENSION.put(merchant.getMerchantId(), jsapiServiceExtension);
        return jsapiServiceExtension;
    }

    public static synchronized NativePayService getNativePayService(WeChatPayProperties weChatPayProperties,
                                                             String entrance) {
        WeChatPayProperties.Merchant merchant = getMerchant(weChatPayProperties, entrance);
        if (NATIVE_PAY_SERVICE.containsKey(merchant.getMerchantId())) {
            return NATIVE_PAY_SERVICE.get(merchant.getMerchantId());
        }
        RSAAutoCertificateConfig rsaAutoCertificateConfig = getRsaAutoCertificateConfig(weChatPayProperties, entrance);
        NativePayService nativePayService = new NativePayService.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
        NATIVE_PAY_SERVICE.put(merchant.getMerchantId(), nativePayService);
        return nativePayService;
    }
}
