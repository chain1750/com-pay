package com.chaincat.pay.service;

/**
 * 业务Service
 *
 * @author chenhaizhuang
 */
public interface ScheduleService {

    /**
     * 处理未支付
     */
    void handleNotPay();

    /**
     * 处理退款中
     */
    void handleInRefund();
}
