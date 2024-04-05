package com.chaincat.pay.controller;

import com.chaincat.pay.model.IResult;
import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.QueryPayResp;
import com.chaincat.pay.model.resp.RefundResp;
import com.chaincat.pay.service.BizService;
import com.chaincat.pay.utils.IResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 业务接口
 *
 * @author chenhaizhuang
 */
@RestController
@RequestMapping("/biz")
public class BizController {

    @Autowired
    private BizService bizService;

    /**
     * 预支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/prepay")
    public IResult<PrepayResp> prepay(@Valid @RequestBody PrepayReq req) {
        return IResultUtils.success(bizService.prepay(req));
    }

    /**
     * 关闭支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/closePay")
    public IResult<Void> closePay(@Valid @RequestBody ClosePayReq req) {
        bizService.closePay(req);
        return IResultUtils.success();
    }

    /**
     * 查询支付
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/queryPay")
    public IResult<QueryPayResp> queryPay(@Valid @RequestBody QueryPayReq req) {
        return IResultUtils.success(bizService.queryPay(req));
    }

    /**
     * 退款
     *
     * @param req 请求
     * @return IResult
     */
    @PostMapping("/refund")
    public IResult<RefundResp> refund(@Valid @RequestBody RefundReq req) {
        return IResultUtils.success(bizService.refund(req));
    }
}
