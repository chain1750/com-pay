package com.chaincat.pay.service;

import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.QueryPayResp;
import com.chaincat.pay.model.resp.RefundResp;

import javax.servlet.http.HttpServletRequest;

/**
 * 业务Service
 *
 * @author chenhaizhuang
 */
public interface BizService {

    /**
     * 预支付
     *
     * @param req 请求
     * @return PrepayResp
     */
    PrepayResp prepay(PrepayReq req);

    /**
     * 关闭支付
     *
     * @param req 请求
     */
    void closePay(ClosePayReq req);

    /**
     * 查询支付
     *
     * @param req 请求
     * @return QueryPayResp
     */
    QueryPayResp queryPay(QueryPayReq req);

    /**
     * 支付通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return String
     */
    String payNotify(HttpServletRequest request, String entrance);

    /**
     * 退款
     *
     * @param req 请求
     * @return RefundResp
     */
    RefundResp refund(RefundReq req);

    /**
     * 退款通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return String
     */
    String refundNotify(HttpServletRequest request, String entrance);
}
