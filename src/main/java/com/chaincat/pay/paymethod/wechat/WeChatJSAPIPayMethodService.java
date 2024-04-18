package com.chaincat.pay.paymethod.wechat;

import cn.hutool.core.lang.Assert;
import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.enums.ThirdpartyTypeEnum;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.feign.baseuser.ThirdpartyUserClient;
import com.chaincat.pay.feign.baseuser.req.ThirdpartyUserOpenIdGetReq;
import com.chaincat.pay.feign.baseuser.resp.ThirdpartyUserOpenIdGetResp;
import com.chaincat.pay.model.IResult;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.wechat.config.WeChatPayFactory;
import com.chaincat.pay.paymethod.wechat.config.WeChatPayProperties;
import com.chaincat.pay.utils.IResultUtils;
import com.wechat.pay.java.core.util.GsonUtil;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneOffset;

/**
 * 微信JSAPI支付方式Service
 *
 * @author chenhaizhuang
 */
@Service("wechatJSAPI")
public class WeChatJSAPIPayMethodService extends WeChatPayMethodService {

    @Autowired
    private ThirdpartyUserClient thirdpartyUserClient;

    @Autowired
    public void setWeChatPayProperties(WeChatPayProperties weChatPayProperties) {
        this.weChatPayProperties = weChatPayProperties;
    }

    @Autowired
    public void setNotifyUrlProperties(NotifyUrlProperties notifyUrlProperties) {
        this.notifyUrlProperties = notifyUrlProperties;
    }

    @Override
    @SuppressWarnings("all")
    public String prepay(PayTransaction payTransaction) {
        String entrance = payTransaction.getEntrance();

        Amount amount = new Amount();
        amount.setTotal(payTransaction.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        Payer payer = new Payer();
        payer.setOpenid(getOpenId(payTransaction));

        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(weChatPayProperties.getEntrances().get(entrance));
        prepayRequest.setMchid(WeChatPayFactory.getMerchant(weChatPayProperties, entrance).getMerchantId());
        prepayRequest.setDescription(payTransaction.getDescription());
        prepayRequest.setOutTradeNo(payTransaction.getTransactionId());
        prepayRequest.setTimeExpire(payTransaction.getExpireTime().atOffset(ZoneOffset.of("+08:00")).format(FORMATTER));
        prepayRequest.setNotifyUrl(notifyUrlProperties.getPayNotifyUrl(entrance));
        prepayRequest.setAmount(amount);
        prepayRequest.setPayer(payer);

        try {
            PrepayWithRequestPaymentResponse prepayResponse = WeChatPayFactory
                    .getJsapiServiceExtension(weChatPayProperties, entrance)
                    .prepayWithRequestPayment(prepayRequest);
            return GsonUtil.toJson(prepayResponse);
        } catch (Exception e) {
            throw new CustomizeException("微信JSAPI支付 预支付失败", e);
        }
    }

    /**
     * 获取OpenId
     *
     * @param payTransaction 支付交易
     * @return String
     */
    private String getOpenId(PayTransaction payTransaction) {
        String entrance = payTransaction.getEntrance();
        String appId = weChatPayProperties.getEntrances().get(entrance);

        ThirdpartyUserOpenIdGetReq req = new ThirdpartyUserOpenIdGetReq();
        req.setUserId(payTransaction.getUserId());
        req.setThirdpartyType(ThirdpartyTypeEnum.WECHAT.getValue());
        req.setAppId(appId);
        IResult<ThirdpartyUserOpenIdGetResp> result = thirdpartyUserClient.getThirdpartyUserOpenId(req);
        Assert.isTrue(IResultUtils.isSuccess(result), "微信JSAPI支付 预支付失败：" + result.getMsg());
        return result.getData().getOpenId();
    }

    @Override
    public void closePay(PayTransaction payTransaction) {
        String entrance = payTransaction.getEntrance();

        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(WeChatPayFactory.getMerchant(weChatPayProperties, entrance).getMerchantId());
        closeOrderRequest.setOutTradeNo(payTransaction.getTransactionId());

        try {
            WeChatPayFactory.getJsapiServiceExtension(weChatPayProperties, entrance).closeOrder(closeOrderRequest);
        } catch (Exception e) {
            throw new CustomizeException("微信JSAPI支付 关闭支付失败", e);
        }
    }

    @Override
    public TransactionResultDTO queryPay(PayTransaction payTransaction) {
        String entrance = payTransaction.getEntrance();

        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();
        queryOrderByOutTradeNoRequest.setMchid(WeChatPayFactory.getMerchant(weChatPayProperties, entrance)
                .getMerchantId());
        queryOrderByOutTradeNoRequest.setOutTradeNo(payTransaction.getTransactionId());

        try {
            Transaction transaction = WeChatPayFactory.getJsapiServiceExtension(weChatPayProperties, entrance)
                    .queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);

            return buildTransactionResult(transaction);
        } catch (Exception e) {
            throw new CustomizeException("微信JSAPI支付 查询支付失败", e);
        }
    }
}
