package com.chaincat.pay.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;

/**
 * ID工具
 *
 * @author chenhaizhuang
 */
public class IdUtils {

    /**
     * 生成交易ID
     *
     * @param prefix 前缀
     * @param now    时间
     * @return String
     */
    public static String generateTransactionId(String prefix, LocalDateTime now) {
        Assert.isTrue(StrUtil.isNumeric(prefix) && prefix.length() == 5);
        return prefix + now.format(DatePattern.PURE_DATE_FORMATTER) + IdUtil.getSnowflakeNextIdStr();
    }
}
