package com.chaincat.pay.strategy;

import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.model.dto.TransactionResultDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * 策略选择器
 *
 * @author chenhaizhuang
 */
public interface StrategySelector {

    /**
     * 预支付
     *
     * @param payTransaction 支付交易
     * @param entrance       入口
     * @return String
     */
    String prepay(PayTransaction payTransaction, String entrance);

    /**
     * 关闭支付
     *
     * @param payTransaction 支付交易
     * @param entrance       入口
     */
    void closePay(PayTransaction payTransaction, String entrance);

    /**
     * 查询支付
     *
     * @param payTransaction 支付交易
     * @param entrance       入口
     * @return TransactionResultDTO
     */
    TransactionResultDTO queryPay(PayTransaction payTransaction, String entrance);

    /**
     * 解析支付通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return TransactionResultDTO
     */
    TransactionResultDTO parsePayNotify(HttpServletRequest request, String entrance);

    /**
     * 退款
     *
     * @param refundTransaction 退款交易
     * @param entrance          入口
     */
    void refund(RefundTransaction refundTransaction, String entrance);

    /**
     * 查询退款
     *
     * @param refundTransaction 退款交易
     * @param entrance          入口
     * @return TransactionResultDTO
     */
    TransactionResultDTO queryRefund(RefundTransaction refundTransaction, String entrance);

    /**
     * 解析退款通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return TransactionResultDTO
     */
    TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance);
}
