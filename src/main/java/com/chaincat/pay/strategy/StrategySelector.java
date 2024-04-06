package com.chaincat.pay.strategy;

import cn.hutool.core.util.StrUtil;
import com.chaincat.pay.config.EntranceProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
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
public class StrategySelector implements GlobalPayMethodService {

    @Autowired
    private EntranceProperties entranceProperties;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 统一支付方式Service
     *
     * @param entrance 入口
     * @return TransactionService
     */
    private GlobalPayMethodService select(String entrance) {
        Map<String, String> map = entranceProperties.getEntrances();
        String payMethod = entranceProperties.getEntrances().getOrDefault(entrance, "");
        if (StrUtil.isEmpty(payMethod) || !applicationContext.containsBean(payMethod)) {
            throw new CustomizeException("入口没有可用的支付方式");
        }
        return applicationContext.getBean(map.get(entrance), GlobalPayMethodService.class);
    }

    @Override
    public String prepay(PayTransaction payTransaction) {
        return select(payTransaction.getEntrance()).prepay(payTransaction);
    }

    @Override
    public void closePay(PayTransaction payTransaction) {
        select(payTransaction.getEntrance()).closePay(payTransaction);
    }

    @Override
    public TransactionResultDTO queryPay(PayTransaction payTransaction) {
        return select(payTransaction.getEntrance()).queryPay(payTransaction);
    }

    @Override
    public TransactionResultDTO parsePayNotify(HttpServletRequest request, String entrance) {
        return select(entrance).parsePayNotify(request, entrance);
    }

    @Override
    public void refund(RefundTransaction refundTransaction) {
        select(refundTransaction.getPayTransaction().getEntrance()).refund(refundTransaction);
    }

    @Override
    public TransactionResultDTO queryRefund(RefundTransaction refundTransaction) {
        return select(refundTransaction.getPayTransaction().getEntrance()).queryRefund(refundTransaction);
    }

    @Override
    public TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance) {
        return select(entrance).parseRefundNotify(request, entrance);
    }
}
