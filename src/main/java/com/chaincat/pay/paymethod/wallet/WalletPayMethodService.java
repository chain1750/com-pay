package com.chaincat.pay.paymethod.wallet;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.feign.baseuser.WalletClient;
import com.chaincat.pay.model.IResult;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.paymethod.wallet.config.WalletPayProperties;
import com.chaincat.pay.paymethod.wallet.model.req.PrepayReq;
import com.chaincat.pay.paymethod.wallet.model.req.QueryReq;
import com.chaincat.pay.paymethod.wallet.model.req.RefundReq;
import com.chaincat.pay.paymethod.wallet.model.resp.PrepayResp;
import com.chaincat.pay.paymethod.wallet.model.resp.TransactionResp;
import com.chaincat.pay.paymethod.wallet.utils.SignUtils;
import com.chaincat.pay.utils.IResultUtils;
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
        PrepayReq req = new PrepayReq();
        req.setUserId(payTransaction.getUserId());
        req.setOutTransactionId(payTransaction.getTransactionId());
        req.setAmount(payTransaction.getAmount());
        req.setDescription(payTransaction.getDescription());
        req.setExpireTime(payTransaction.getExpireTime());
        req.setNotifyUrl(notifyUrlProperties.getPayNotifyUrl(payTransaction.getEntrance()));

        IResult<PrepayResp> result = walletClient.prepay(req);
        IResultUtils.checkAndThrow(result);
        return JSON.toJSONString(result.getData());
    }

    @Override
    public void closePay(PayTransaction payTransaction) {
        QueryReq req = new QueryReq();
        req.setOutTransactionId(payTransaction.getTransactionId());

        IResult<Void> result = walletClient.closePay(req);
        IResultUtils.checkAndThrow(result);
    }

    @Override
    public TransactionResultDTO queryPay(PayTransaction payTransaction) {
        QueryReq req = new QueryReq();
        req.setOutTransactionId(payTransaction.getTransactionId());

        IResult<TransactionResp> result = walletClient.queryPay(req);
        IResultUtils.checkAndThrow(result);
        TransactionResp transactionResp = result.getData();

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(transactionResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(transactionResp.getTransactionId());
        transactionResult.setStatus(transactionResp.getStatus());
        transactionResult.setFinishTime(transactionResp.getFinishTime());
        return transactionResult;
    }

    @Override
    public TransactionResultDTO parsePayNotify(HttpServletRequest request, String entrance) {
        String body = ServletUtil.getBody(request);
        JSONObject requestBody = JSON.parseObject(body);
        SignUtils.verify(requestBody, walletPayProperties.getSalt(), walletPayProperties.getSignParamKey());
        TransactionResp transactionResp = JSON.parseObject(body, TransactionResp.class);

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(transactionResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(transactionResp.getTransactionId());
        transactionResult.setStatus(transactionResp.getStatus());
        transactionResult.setFinishTime(transactionResp.getFinishTime());
        return transactionResult;
    }

    @Override
    public void refund(RefundTransaction refundTransaction) {
        PayTransaction payTransaction = refundTransaction.getPayTransaction();

        RefundReq req = new RefundReq();
        req.setPayOutTransactionId(payTransaction.getTransactionId());
        req.setOutTransactionId(refundTransaction.getTransactionId());
        req.setAmount(refundTransaction.getAmount());
        req.setReason(refundTransaction.getDescription());
        req.setNotifyUrl(notifyUrlProperties.getRefundNotifyUrl(payTransaction.getEntrance()));

        IResult<Void> result = walletClient.refund(req);
        IResultUtils.checkAndThrow(result);
    }

    @Override
    public TransactionResultDTO queryRefund(RefundTransaction refundTransaction) {
        QueryReq req = new QueryReq();
        req.setOutTransactionId(refundTransaction.getTransactionId());

        IResult<TransactionResp> result = walletClient.queryRefund(req);
        IResultUtils.checkAndThrow(result);
        TransactionResp transactionResp = result.getData();

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(transactionResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(transactionResp.getTransactionId());
        transactionResult.setStatus(transactionResp.getStatus());
        transactionResult.setFinishTime(transactionResp.getFinishTime());
        return transactionResult;
    }

    @Override
    public TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance) {
        String body = ServletUtil.getBody(request);
        JSONObject requestBody = JSON.parseObject(body);
        SignUtils.verify(requestBody, walletPayProperties.getSalt(), walletPayProperties.getSignParamKey());
        TransactionResp transactionResp = JSON.parseObject(body, TransactionResp.class);

        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(transactionResp.getOutTransactionId());
        transactionResult.setPayMethodTransactionId(transactionResp.getTransactionId());
        transactionResult.setStatus(transactionResp.getStatus());
        transactionResult.setFinishTime(transactionResp.getFinishTime());
        return transactionResult;
    }
}
