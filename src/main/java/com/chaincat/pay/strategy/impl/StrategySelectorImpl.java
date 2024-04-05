package com.chaincat.pay.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.chaincat.pay.config.EntranceProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.strategy.StrategySelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 策略选择器
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
public class StrategySelectorImpl implements StrategySelector {

    @Autowired
    private EntranceProperties entranceProperties;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 统一支付方式Service
     *
     * @param entrance 支付入口
     * @return TransactionService
     */
    private GlobalPayMethodService select(String entrance) {
        Map<String, String> map = entranceProperties.getEntrances();
        String payMethod = entranceProperties.getEntrances().getOrDefault(entrance, "");
        if (StrUtil.isEmpty(payMethod) || !applicationContext.containsBean(payMethod)) {
            throw new CustomizeException("支付入口没有可用的支付方式");
        }
        return applicationContext.getBean(map.get(entrance), GlobalPayMethodService.class);
    }

    @Override
    public String prepay(PayTransaction payTransaction, String entrance) {
        return select(entrance).prepay(payTransaction);
    }

    @Override
    public void closePay(PayTransaction payTransaction, String entrance) {
        select(entrance).closePay(payTransaction);
    }

    @Override
    public TransactionResultDTO queryPay(PayTransaction payTransaction, String entrance) {
        return select(entrance).queryPay(payTransaction);
    }

    @Override
    public TransactionResultDTO parsePayNotify(HttpServletRequest request, String entrance) {
        return select(entrance).parsePayNotify(request);
    }

    @Override
    public void refund(RefundTransaction refundTransaction, String entrance) {
        select(entrance).refund(refundTransaction);
    }

    @Override
    public TransactionResultDTO queryRefund(RefundTransaction refundTransaction, String entrance) {
        return select(entrance).queryRefund(refundTransaction);
    }

    @Override
    public TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance) {
        return select(entrance).parseRefundNotify(request);
    }
}
