package com.chaincat.pay.controller;

import com.chaincat.pay.model.IResult;
import com.chaincat.pay.service.BizService;
import com.chaincat.pay.utils.IResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 通知接口
 *
 * @author chenhaizhuang
 */
@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Autowired
    private BizService bizService;

    /**
     * 支付通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return IResult
     */
    @PostMapping("/pay/{entrance}")
    public IResult<String> payNotify(HttpServletRequest request, @PathVariable String entrance) {
        return IResultUtils.success(bizService.payNotify(request, entrance));
    }

    /**
     * 退款通知
     *
     * @param request  通知请求
     * @param entrance 入口
     * @return IResult
     */
    @PostMapping("/refund/{entrance}")
    public IResult<String> refundNotify(HttpServletRequest request, @PathVariable String entrance) {
        return IResultUtils.success(bizService.refundNotify(request, entrance));
    }
}
