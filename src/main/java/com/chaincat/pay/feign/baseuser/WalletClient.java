package com.chaincat.pay.feign.baseuser;

import com.chaincat.pay.feign.baseuser.req.WalletClosePayReq;
import com.chaincat.pay.feign.baseuser.req.WalletQueryRefundReq;
import com.chaincat.pay.feign.baseuser.resp.WalletPayResp;
import com.chaincat.pay.model.IResult;
import com.chaincat.pay.feign.baseuser.req.WalletPrepayReq;
import com.chaincat.pay.feign.baseuser.req.WalletQueryPayReq;
import com.chaincat.pay.feign.baseuser.req.WalletRefundReq;
import com.chaincat.pay.feign.baseuser.resp.WalletPrepayResp;
import com.chaincat.pay.feign.baseuser.resp.WalletRefundResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 钱包Client
 *
 * @author chenhaizhuang
 */
@FeignClient(name = "base-user", path = "/base/user/wallet",
        contextId = "com.chaincat.pay.feign.baseuser.WalletClient")
public interface WalletClient {

    /**
     * 预支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/prepay")
    IResult<WalletPrepayResp> prepay(WalletPrepayReq req);

    /**
     * 关闭支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/closePay")
    IResult<Void> closePay(WalletClosePayReq req);

    /**
     * 查询支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/queryPay")
    IResult<WalletPayResp> queryPay(WalletQueryPayReq req);

    /**
     * 退款
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/refund")
    IResult<Void> refund(WalletRefundReq req);

    /**
     * 查询退款
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/queryRefund")
    IResult<WalletRefundResp> queryRefund(WalletQueryRefundReq req);
}
