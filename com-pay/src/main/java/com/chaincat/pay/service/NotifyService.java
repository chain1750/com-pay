package com.chaincat.pay.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 通知Service
 *
 * @author chenhaizhuang
 */
public interface NotifyService {

    /**
     * 支付通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return String
     */
    String payNotify(HttpServletRequest request, String entrance);

    /**
     * 退款通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return String
     */
    String refundNotify(HttpServletRequest request, String entrance);
}
