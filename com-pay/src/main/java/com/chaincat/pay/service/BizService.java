package com.chaincat.pay.service;

import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.QueryPayResp;
import com.chaincat.pay.model.resp.RefundResp;

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
     * 退款
     *
     * @param req 请求
     * @return RefundResp
     */
    RefundResp refund(RefundReq req);
}
