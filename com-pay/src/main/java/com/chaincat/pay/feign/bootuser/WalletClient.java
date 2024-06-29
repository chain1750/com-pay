package com.chaincat.pay.feign.bootuser;

import com.chaincat.pay.feign.bootuser.req.WalletClosePayReq;
import com.chaincat.pay.feign.bootuser.req.WalletPrepayReq;
import com.chaincat.pay.feign.bootuser.req.WalletQueryPayReq;
import com.chaincat.pay.feign.bootuser.req.WalletQueryRefundReq;
import com.chaincat.pay.feign.bootuser.req.WalletRefundReq;
import com.chaincat.pay.feign.bootuser.resp.WalletPayResp;
import com.chaincat.pay.feign.bootuser.resp.WalletPrepayResp;
import com.chaincat.pay.feign.bootuser.resp.WalletRefundResp;
import com.chaincat.pay.model.IResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 钱包Client
 *
 * @author chenhaizhuang
 */
@FeignClient(name = "boot-user", path = "/boot/user/wallet",
        contextId = "com.chaincat.pay.feign.bootuser.WalletClient")
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
