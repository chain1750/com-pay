package com.chaincat.pay.paymethod.alipay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.enums.ThirdpartyTypeEnum;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.feign.bootuser.ThirdpartyUserClient;
import com.chaincat.pay.feign.bootuser.req.ThirdpartyUserOpenIdGetReq;
import com.chaincat.pay.feign.bootuser.resp.ThirdpartyUserOpenIdGetResp;
import com.chaincat.pay.model.IResult;
import com.chaincat.pay.paymethod.alipay.config.AlipayPayFactory;
import com.chaincat.pay.paymethod.alipay.config.AlipayPayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付宝JSAPI支付方式Service
 *
 * @author chenhaizhuang
 */
@Service("alipayJSAPI")
public class AlipayJSAPIPayMethodService extends AlipayPayMethodService {

    @Autowired
    private ThirdpartyUserClient thirdpartyUserClient;

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

        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setOutTradeNo(payTransaction.getTransactionId());
        model.setTotalAmount(payTransaction.getAmount().toString());
        model.setSubject(payTransaction.getDescription());
        model.setProductCode("JSAPI_PAY");
        model.setBuyerOpenId(getOpenId(payTransaction));
        model.setOpAppId(alipayPayProperties.getEntrances().get(entrance));
        model.setTimeExpire(payTransaction.getExpireTime().format(DatePattern.NORM_DATETIME_FORMATTER));
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setNotifyUrl(notifyUrlProperties.getPayNotifyUrl(entrance));
        request.setBizModel(model);

        AlipayTradeCreateResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new CustomizeException("支付宝小程序支付 预支付失败", e);
        }
        Assert.isTrue(response.isSuccess(), "支付宝小程序支付 预支付失败：" + response.getSubMsg());
        return response.getBody();
    }

    /**
     * 获取OpenId
     *
     * @param payTransaction 支付交易
     * @return String
     */
    private String getOpenId(PayTransaction payTransaction) {
        String entrance = payTransaction.getEntrance();
        String appId = alipayPayProperties.getEntrances().get(entrance);

        ThirdpartyUserOpenIdGetReq req = new ThirdpartyUserOpenIdGetReq();
        req.setUserId(payTransaction.getUserId());
        req.setThirdpartyType(ThirdpartyTypeEnum.ALIPAY.getValue());
        req.setAppId(appId);
        IResult<ThirdpartyUserOpenIdGetResp> result = thirdpartyUserClient.getThirdpartyUserOpenId(req);
        Assert.isTrue(result.isSuccess(), "支付宝小程序支付 预支付失败：" + result.getMsg());
        return result.getData().getOpenId();
    }
}
