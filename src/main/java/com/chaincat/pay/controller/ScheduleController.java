package com.chaincat.pay.controller;

import com.chaincat.components.pay.common.model.IResult;
import com.chaincat.components.pay.common.utils.IResultUtils;
import com.chaincat.components.pay.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知接口
 *
 * @author chenhaizhuang
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 处理未支付
     *
     * @return IResult
     */
    @PostMapping("/handleNotPay")
    public IResult<Void> handleNotPay() {
        scheduleService.handleNotPay();
        return IResultUtils.success();
    }

    /**
     * 处理退款中
     *
     * @return IResult
     */
    @PostMapping("/handleInRefund")
    public IResult<Void> handleInRefund() {
        scheduleService.handleInRefund();
        return IResultUtils.success();
    }
}
