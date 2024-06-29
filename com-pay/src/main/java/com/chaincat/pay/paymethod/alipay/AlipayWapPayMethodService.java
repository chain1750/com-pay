package com.chaincat.pay.paymethod.alipay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.paymethod.alipay.config.AlipayPayFactory;
import com.chaincat.pay.paymethod.alipay.config.AlipayPayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付宝Wap支付方式Service
 *
 * @author chenhaizhuang
 */
@Service("alipayWap")
public class AlipayWapPayMethodService extends AlipayPayMethodService {

    @Autowired
    public void setAlipayPayProperties(AlipayPayProperties alipayPayProperties) {
        this.alipayPayProperties = alipayPayProperties;
    }

    @Autowired
    public void setNotifyUrlProperties(NotifyUrlProperties notifyUrlProperties) {
        this.notifyUrlProperties = notifyUrlProperties;
    }

    @Override
    public String prepay(PayTransaction payTransaction) {
        String entrance = payTransaction.getEntrance();
        AlipayClient alipayClient = AlipayPayFactory.getAlipayClient(alipayPayProperties, entrance);

        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payTransaction.getTransactionId());
        model.setTotalAmount(payTransaction.getAmount().toString());
        model.setSubject(payTransaction.getDescription());
        model.setProductCode("QUICK_WAP_WAY");
        model.setSellerId(AlipayPayFactory.getAccount(alipayPayProperties, entrance).getSellerId());
        model.setTimeExpire(payTransaction.getExpireTime().format(DatePattern.NORM_DATETIME_FORMATTER));
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(notifyUrlProperties.getPayNotifyUrl(entrance));
        request.setBizModel(model);

        AlipayTradeWapPayResponse response;
        try {
            response = alipayClient.pageExecute(request);
        } catch (Exception e) {
            throw new CustomizeException("支付宝手机网站支付 预支付失败", e);
        }
        Assert.isTrue(response.isSuccess(), "支付宝手机网站支付 预支付失败：" + response.getSubMsg());
        return response.getBody();
    }
}
