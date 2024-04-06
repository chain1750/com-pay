package com.chaincat.pay.feign.baseuser;

import com.chaincat.pay.model.IResult;
import com.chaincat.pay.paymethod.wallet.model.req.PrepayReq;
import com.chaincat.pay.paymethod.wallet.model.req.QueryReq;
import com.chaincat.pay.paymethod.wallet.model.req.RefundReq;
import com.chaincat.pay.paymethod.wallet.model.resp.PrepayResp;
import com.chaincat.pay.paymethod.wallet.model.resp.TransactionResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 钱包Client
 *
 * @author chenhaizhuang
 */
@FeignClient(name = "base-user", path = "/base/user/wallet",
        contextId = "com.chaincat.pay.feign.baseuser.ThirdpartyUserClient")
public interface WalletClient {

    /**
     * 预支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/prepay")
    IResult<PrepayResp> prepay(PrepayReq req);

    /**
     * 关闭支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/closePay")
    IResult<Void> closePay(QueryReq req);

    /**
     * 查询支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/queryPay")
    IResult<TransactionResp> queryPay(QueryReq req);

    /**
     * 退款
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/refund")
    IResult<Void> refund(RefundReq req);

    /**
     * 查询退款
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/queryRefund")
    IResult<TransactionResp> queryRefund(QueryReq req);
}
