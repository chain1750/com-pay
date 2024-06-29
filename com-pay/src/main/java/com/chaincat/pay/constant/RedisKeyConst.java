package com.chaincat.pay.constant;

/**
 * Redis键常量
 *
 * @author chenhaizhuang
 */
public interface RedisKeyConst {

    /**
     * 预支付锁
     */
    String LOCK_PREPAY = "pay:lock:prepay:{}:{}";

    /**
     * 支付交易锁
     */
    String LOCK_PAY = "pay:lock:pay:{}";

    /**
     * 退款执行锁
     */
    String LOCK_REFUND_EXEC = "pay:lock:refund:exec:{}";

    /**
     * 退款查询锁
     */
    String LOCK_REFUND_QUERY = "pay:lock:refund:query:{}";
}
