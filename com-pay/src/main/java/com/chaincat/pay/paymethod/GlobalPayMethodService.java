package com.chaincat.pay.paymethod;

import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.model.dto.TransactionResultDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一支付方式Service
 *
 * @author chenhaizhuang
 */
public interface GlobalPayMethodService {

    /**
     * 预支付
     *
     * @param payTransaction 支付交易
     * @return String
     */
    String prepay(PayTransaction payTransaction);

    /**
     * 关闭支付
     *
     * @param payTransaction 支付交易
     */
    void closePay(PayTransaction payTransaction);

    /**
     * 查询支付
     *
     * @param payTransaction 支付交易
     * @return TransactionResultDTO
     */
    TransactionResultDTO queryPay(PayTransaction payTransaction);

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
     */
    void refund(RefundTransaction refundTransaction);

    /**
     * 查询退款
     *
     * @param refundTransaction 退款交易
     * @return TransactionResultDTO
     */
    TransactionResultDTO queryRefund(RefundTransaction refundTransaction);

    /**
     * 解析退款通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return TransactionResultDTO
     */
    TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance);
}
