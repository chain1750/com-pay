package com.chaincat.pay.paymethod.wallet;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.feign.bootuser.WalletClient;
import com.chaincat.pay.feign.bootuser.req.WalletClosePayReq;
import com.chaincat.pay.feign.bootuser.req.WalletPrepayReq;
import com.chaincat.pay.feign.bootuser.req.WalletQueryPayReq;
import com.chaincat.pay.feign.bootuser.req.WalletQueryRefundReq;
import com.chaincat.pay.feign.bootuser.req.WalletRefundReq;
import com.chaincat.pay.feign.bootuser.resp.WalletPayResp;
import com.chaincat.pay.feign.bootuser.resp.WalletPrepayResp;
import com.chaincat.pay.feign.bootuser.resp.WalletRefundResp;
import com.chaincat.pay.model.IResult;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.paymethod.wallet.config.WalletPayProperties;
import com.chaincat.pay.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 钱包支付方式Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service("wallet")
public class WalletPayMethodService implements GlobalPayMethodService {

    @Autowired
    private WalletPayProperties walletPayProperties;

    @Autowired
    private NotifyUrlProperties notifyUrlProperties;

    @Autowired
    private WalletClient walletClient;

    @Override
    public String prepay(PayTransaction payTransaction) {
        WalletPrepayReq req = new WalletPrepayReq();
        req.setUserId(payTransaction.getUserId());
        req.setOutTransactionId(payTransaction.getTransactionId());
        req.setAmount(payTransaction.getAmount());
        req.setDescription(payTransaction.getDescription());
        req.setExpireTime(payTransaction.getExpireTime());
        req.setNotifyUrl(notifyUrlProperties.getPayNotifyUrl(payTransaction.getEntrance()));

        IResult<WalletPrepayResp> result = walletClient.prepay(req);
        Assert.isTrue(result.isSuccess(), "钱包支付 预支付失败：" + result.getMsg());
        return JSON.toJSONString(result.getData());
    }

    @Override
    public void closePay(PayTransaction payTransaction) {
        WalletClosePayReq req = new WalletClosePayReq();
        req.setOutTransactionId(payTransaction.getTransactionId());

        IResult<Void> result = walletClient.closePay(req);
        Assert.isTrue(result.isSuccess(), "钱包支付 关闭支付失败：" + result.getMsg());
    }

    @Override
    public TransactionResultDTO queryPay(PayTransaction payTransaction) {
        WalletQueryPayReq req = new WalletQueryPayReq();
        req.setOutTransactionId(payTransaction.getTransactionId());

        IResult<WalletPayResp> result = walletClient.queryPay(req);
        Assert.isTrue(result.isSuccess(), "钱包支付 查询支付失败：" + result.getMsg());
        WalletPayResp walletPayResp = result.getData();

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(walletPayResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(walletPayResp.getTransactionId());
        transactionResult.setStatus(walletPayResp.getStatus());
        transactionResult.setFinishTime(walletPayResp.getFinishTime());
        return transactionResult;
    }

    @Override
    public TransactionResultDTO parsePayNotify(HttpServletRequest request, String entrance) {
        String body = ServletUtil.getBody(request);
        JSONObject requestBody = JSON.parseObject(body);
        boolean verify =
                SignUtils.verify(requestBody, walletPayProperties.getSalt(), walletPayProperties.getSignParamKey());
        Assert.isTrue(verify, "钱包支付 解析支付通知失败");
        WalletPayResp walletPayResp = JSON.parseObject(body, WalletPayResp.class);

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(walletPayResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(walletPayResp.getTransactionId());
        transactionResult.setStatus(walletPayResp.getStatus());
        transactionResult.setFinishTime(walletPayResp.getFinishTime());
        return transactionResult;
    }

    @Override
    public void refund(RefundTransaction refundTransaction) {
        PayTransaction payTransaction = refundTransaction.getPayTransaction();

        WalletRefundReq req = new WalletRefundReq();
        req.setPayOutTransactionId(payTransaction.getTransactionId());
        req.setOutTransactionId(refundTransaction.getTransactionId());
        req.setAmount(refundTransaction.getAmount());
        req.setReason(refundTransaction.getDescription());
        req.setNotifyUrl(notifyUrlProperties.getRefundNotifyUrl(payTransaction.getEntrance()));

        IResult<Void> result = walletClient.refund(req);
        Assert.isTrue(result.isSuccess(), "钱包支付 退款失败：" + result.getMsg());
    }

    @Override
    public TransactionResultDTO queryRefund(RefundTransaction refundTransaction) {
        WalletQueryRefundReq req = new WalletQueryRefundReq();
        req.setOutTransactionId(refundTransaction.getTransactionId());

        IResult<WalletRefundResp> result = walletClient.queryRefund(req);
        Assert.isTrue(result.isSuccess(), "钱包支付 查询退款失败：" + result.getMsg());
        WalletRefundResp walletRefundResp = result.getData();

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(walletRefundResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(walletRefundResp.getTransactionId());
        transactionResult.setStatus(walletRefundResp.getStatus());
        transactionResult.setFinishTime(walletRefundResp.getFinishTime());
        return transactionResult;
    }

    @Override
    public TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance) {
        String body = ServletUtil.getBody(request);
        JSONObject requestBody = JSON.parseObject(body);
        boolean verify =
                SignUtils.verify(requestBody, walletPayProperties.getSalt(), walletPayProperties.getSignParamKey());
        Assert.isTrue(verify, "钱包支付 解析退款通知失败");
        WalletRefundResp walletRefundResp = JSON.parseObject(body, WalletRefundResp.class);

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(walletRefundResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(walletRefundResp.getTransactionId());
        transactionResult.setStatus(walletRefundResp.getStatus());
        transactionResult.setFinishTime(walletRefundResp.getFinishTime());
        return transactionResult;
    }
}
