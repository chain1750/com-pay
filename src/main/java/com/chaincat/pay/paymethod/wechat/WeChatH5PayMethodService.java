package com.chaincat.pay.paymethod.wechat;

import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.wechat.config.WeChatPayFactory;
import com.chaincat.pay.paymethod.wechat.config.WeChatPayProperties;
import com.wechat.pay.java.core.util.GsonUtil;
import com.wechat.pay.java.service.payments.h5.model.Amount;
import com.wechat.pay.java.service.payments.h5.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.h5.model.H5Info;
import com.wechat.pay.java.service.payments.h5.model.PrepayRequest;
import com.wechat.pay.java.service.payments.h5.model.PrepayResponse;
import com.wechat.pay.java.service.payments.h5.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.h5.model.SceneInfo;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneOffset;

/**
 * 微信H5支付方式Service
 *
 * @author chenhaizhuang
 */
@Service("wechatH5")
public class WeChatH5PayMethodService extends WeChatPayMethodService {

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
        H5Info h5Info = new H5Info();
        h5Info.setType("Wap");
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(payTransaction.getUserIp());
        sceneInfo.setH5Info(h5Info);

        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(weChatPayProperties.getEntrances().get(entrance));
        prepayRequest.setMchid(WeChatPayFactory.getMerchant(weChatPayProperties, entrance).getMerchantId());
        prepayRequest.setDescription(payTransaction.getDescription());
        prepayRequest.setOutTradeNo(payTransaction.getTransactionId());
        prepayRequest.setTimeExpire(payTransaction.getExpireTime().atOffset(ZoneOffset.of("+08:00")).format(FORMATTER));
        prepayRequest.setNotifyUrl(notifyUrlProperties.getPayNotifyUrl(entrance));
        prepayRequest.setAmount(amount);
        prepayRequest.setSceneInfo(sceneInfo);

        try {
            PrepayResponse prepayResponse = WeChatPayFactory.getH5Service(weChatPayProperties, entrance)
                    .prepay(prepayRequest);
            return GsonUtil.toJson(prepayResponse);
        } catch (Exception e) {
            throw new CustomizeException("微信H5支付 预支付失败", e);
        }
    }

    @Override
    public void closePay(PayTransaction payTransaction) {
        String entrance = payTransaction.getEntrance();

        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(WeChatPayFactory.getMerchant(weChatPayProperties, entrance).getMerchantId());
        closeOrderRequest.setOutTradeNo(payTransaction.getTransactionId());

        try {
            WeChatPayFactory.getH5Service(weChatPayProperties, entrance).closeOrder(closeOrderRequest);
        } catch (Exception e) {
            throw new CustomizeException("微信H5支付 关闭支付失败", e);
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
            Transaction transaction = WeChatPayFactory.getH5Service(weChatPayProperties, entrance)
                    .queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);

            return buildTransactionResult(transaction);
        } catch (Exception e) {
            throw new CustomizeException("微信H5支付 查询支付失败", e);
        }
    }
}
