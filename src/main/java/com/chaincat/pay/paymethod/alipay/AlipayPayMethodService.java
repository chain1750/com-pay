package com.chaincat.pay.paymethod.alipay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.servlet.ServletUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.enums.PayStatusEnum;
import com.chaincat.pay.enums.RefundStatusEnum;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.paymethod.alipay.config.AlipayPayFactory;
import com.chaincat.pay.paymethod.alipay.config.AlipayPayProperties;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付宝支付方式Service
 *
 * @author chenhaizhuang
 */
@Slf4j
public abstract class AlipayPayMethodService implements GlobalPayMethodService {

    protected AlipayPayProperties alipayPayProperties;

    protected NotifyUrlProperties notifyUrlProperties;

    @Override
    public void closePay(PayTransaction payTransaction) {
        AlipayClient alipayClient = AlipayPayFactory.getAlipayClient(alipayPayProperties, payTransaction.getEntrance());

        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(payTransaction.getTransactionId());
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);

        AlipayTradeCloseResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new CustomizeException("支付宝支付 关闭支付失败", e);
        }
        Assert.isTrue(response.isSuccess(), "支付宝支付 关闭支付失败：" + response.getSubMsg());
    }

    @Override
    public TransactionResultDTO queryPay(PayTransaction payTransaction) {
        AlipayClient alipayClient = AlipayPayFactory.getAlipayClient(alipayPayProperties, payTransaction.getEntrance());

        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(payTransaction.getTransactionId());
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);

        AlipayTradeQueryResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new CustomizeException("支付宝支付 查询支付失败", e);
        }
        Assert.isTrue(response.isSuccess(), "支付宝支付 查询支付失败：" + response.getSubMsg());

        return buildTransactionResult(response);
    }

    @Override
    public TransactionResultDTO parsePayNotify(HttpServletRequest request, String entrance) {
        Map<String, String> requestParam = new HashMap<>();
        Map<String, String> paramMap = ServletUtil.getParamMap(request);
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String name = entry.getKey();
            if ("sign_type".equals(name)) {
                continue;
            }
            requestParam.put(name, URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8));
        }
        AlipayPayProperties.Account account = AlipayPayFactory.getAccount(alipayPayProperties, entrance);
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV2(requestParam, account.getPublicKey(), "utf-8", "RSA2");
        } catch (Exception e) {
            throw new CustomizeException("支付宝支付 解析支付通知失败", e);
        }
        String sellerId = requestParam.get("seller_id");
        Assert.isTrue(signVerified && account.getSellerId().equals(sellerId), "支付宝支付 解析支付通知失败");

        AlipayTradeQueryResponse response = new AlipayTradeQueryResponse();
        response.setOutTradeNo(requestParam.get("out_trade_no"));
        response.setTradeNo(requestParam.get("trade_no"));
        response.setTradeStatus(requestParam.get("trade_status"));
        response.setSendPayDate(DateUtil.parse(requestParam.get("gmt_payment"), DatePattern.NORM_DATETIME_PATTERN));
        log.info("支付宝支付 支付通知结果：{}", response);
        return buildTransactionResult(response);
    }

    /**
     * 构建支付结果
     *
     * @param response 查询支付结果
     * @return TransactionResultDTO
     */
    private TransactionResultDTO buildTransactionResult(AlipayTradeQueryResponse response) {
        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(response.getOutTradeNo());
        transactionResult.setPayMethodTransactionId(response.getTradeNo());

        String tradeStatus = response.getTradeStatus();
        if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
            transactionResult.setStatus(PayStatusEnum.NOT_PAY.getValue());
        } else if ("TRADE_SUCCESS".equals(tradeStatus)) {
            transactionResult.setStatus(PayStatusEnum.PAY_SUCCESS.getValue());
            transactionResult.setFinishTime(LocalDateTimeUtil.of(response.getSendPayDate()));
        } else {
            transactionResult.setStatus(PayStatusEnum.PAY_CLOSED.getValue());
        }

        transactionResult.setNotifyResult("success");
        return transactionResult;
    }

    @Override
    public void refund(RefundTransaction refundTransaction) {
        PayTransaction payTransaction = refundTransaction.getPayTransaction();

        AlipayClient alipayClient = AlipayPayFactory.getAlipayClient(alipayPayProperties, payTransaction.getEntrance());
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(payTransaction.getTransactionId());
        model.setRefundAmount(refundTransaction.getAmount().toString());
        model.setRefundReason(refundTransaction.getDescription());
        model.setOutRequestNo(refundTransaction.getTransactionId());
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrlProperties.getRefundNotifyUrl(payTransaction.getEntrance()));

        AlipayTradeRefundResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new CustomizeException("支付宝支付 退款失败", e);
        }
        Assert.isTrue(response.isSuccess(), "支付宝支付 退款失败：" + response.getSubMsg());
    }

    @Override
    public TransactionResultDTO queryRefund(RefundTransaction refundTransaction) {
        PayTransaction payTransaction = refundTransaction.getPayTransaction();

        AlipayClient alipayClient = AlipayPayFactory.getAlipayClient(alipayPayProperties, payTransaction.getEntrance());
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(payTransaction.getTransactionId());
        model.setOutRequestNo(refundTransaction.getTransactionId());
        model.setQueryOptions(List.of("gmt_refund_pay"));
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);

        AlipayTradeFastpayRefundQueryResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new CustomizeException("支付宝支付 查询退款失败", e);
        }
        Assert.isTrue(response.isSuccess(), "支付宝支付 查询退款失败：" + response.getSubMsg());
        return buildTransactionResult(response);
    }

    @Override
    public TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance) {
        Map<String, String> requestParam = new HashMap<>();
        Map<String, String> paramMap = ServletUtil.getParamMap(request);
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String name = entry.getKey();
            if ("sign_type".equals(name)) {
                continue;
            }
            requestParam.put(name, URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8));
        }
        AlipayPayProperties.Account account = AlipayPayFactory.getAccount(alipayPayProperties, entrance);
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV2(requestParam, account.getPublicKey(), "utf-8", "RSA2");
        } catch (Exception e) {
            throw new CustomizeException("支付宝支付 解析退款通知失败", e);
        }

        String sellerId = requestParam.get("seller_id");
        Assert.isTrue(signVerified && account.getSellerId().equals(sellerId), "支付宝支付 解析退款通知失败");

        String tradeStatus = requestParam.get("trade_status");
        String refundStatus = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_CLOSED".equals(tradeStatus)
                ? "REFUND_SUCCESS" : null;
        AlipayTradeFastpayRefundQueryResponse response = new AlipayTradeFastpayRefundQueryResponse();
        response.setOutRequestNo(requestParam.get("out_biz_no"));
        response.setRefundStatus(refundStatus);
        response.setGmtRefundPay(DateUtil.parse(requestParam.get("gmt_payment"), DatePattern.NORM_DATETIME_MS_FORMAT));
        log.info("支付宝支付 退款通知结果：{}", response);
        return buildTransactionResult(response);
    }

    /**
     * 构建退款结果
     *
     * @param response 查询退款结果
     * @return TransactionResultDTO
     */
    private TransactionResultDTO buildTransactionResult(AlipayTradeFastpayRefundQueryResponse response) {
        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(response.getOutRequestNo());
        transactionResult.setPayMethodTransactionId(response.getTradeNo());

        String refundStatus = response.getRefundStatus();
        if ("REFUND_SUCCESS".equals(refundStatus)) {
            transactionResult.setStatus(RefundStatusEnum.REFUND_SUCCESS.getValue());
            transactionResult.setFinishTime(LocalDateTimeUtil.of(response.getGmtRefundPay()));
        } else {
            transactionResult.setStatus(RefundStatusEnum.IN_REFUND.getValue());
        }

        transactionResult.setNotifyResult("success");
        return transactionResult;
    }
}
