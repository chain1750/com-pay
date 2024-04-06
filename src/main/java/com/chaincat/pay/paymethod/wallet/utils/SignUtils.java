package com.chaincat.pay.paymethod.wallet.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 签名工具
 *
 * @author chenhaizhuang
 */
public class SignUtils {

    /**
     * 获取签名
     *
     * @param requestMap 请求参数
     * @param salt       盐值
     * @return String
     */
    public static String getSign(Map<String, Object> requestMap, String salt) {
        List<String> paramsArr = new ArrayList<>();
        requestMap.forEach((key, valueObj) -> {
            String value = valueObj.toString().trim();
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                value = value.substring(1, value.length() - 1).trim();
            }
            if (StrUtil.isNotEmpty(value)) {
                paramsArr.add(value);
            }
        });
        paramsArr.add(salt);
        paramsArr.sort(String::compareTo);
        String mergeStr = String.join("&", paramsArr);
        return SecureUtil.md5(mergeStr);
    }

    /**
     * 验证签名
     *
     * @param requestMap   请求参数
     * @param salt         盐值
     * @param signParamKey 签名字段
     * @return boolean
     */
    public static boolean verify(Map<String, Object> requestMap, String salt, String signParamKey) {
        String signParamValue = (String) requestMap.get(signParamKey);
        requestMap.remove(signParamKey);
        String sign = getSign(requestMap, salt);
        return StrUtil.isNotEmpty(signParamValue) && signParamValue.equals(sign);
    }
}
