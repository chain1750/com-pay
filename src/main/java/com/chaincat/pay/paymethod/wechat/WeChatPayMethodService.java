package com.chaincat.pay.paymethod.wechat;

import cn.hutool.core.date.DatePattern;
import cn.hutool.extra.servlet.ServletUtil;
import com.chaincat.pay.config.NotifyUrlProperties;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.enums.PayStatusEnum;
import com.chaincat.pay.enums.RefundStatusEnum;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.paymethod.wechat.config.WeChatPayFactory;
import com.chaincat.pay.paymethod.wechat.config.WeChatPayProperties;
import com.wechat.pay.java.core.http.Constant;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 微信支付方式Service
 *
 * @author chenhaizhuang
 */
@Slf4j
public abstract class WeChatPayMethodService implements GlobalPayMethodService {

    protected static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(DatePattern.UTC_WITH_XXX_OFFSET_PATTERN);

    protected WeChatPayProperties weChatPayProperties;

    protected NotifyUrlProperties notifyUrlProperties;

    @Override
    public TransactionResultDTO parsePayNotify(HttpServletRequest request, String entrance) {
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader(Constant.WECHAT_PAY_SERIAL))
                    .nonce(request.getHeader(Constant.WECHAT_PAY_NONCE))
                    .signature(request.getHeader(Constant.WECHAT_PAY_SIGNATURE))
                    .timestamp(request.getHeader(Constant.WECHAT_PAY_TIMESTAMP))
                    .body(ServletUtil.getBody(request))
                    .build();
            log.info("微信支付 支付通知参数：{}", requestParam);

            Transaction transaction = WeChatPayFactory.getNotificationParser(weChatPayProperties, entrance)
                    .parse(requestParam, Transaction.class);
            log.info("微信支付 支付通知结果：{}", transaction);

            return buildTransactionResult(transaction);
        } catch (Exception e) {
            throw new CustomizeException("微信支付 解析支付通知失败", e);
        }
    }

    /**
     * 构建交易结果
     *
     * @param transaction 支付交易结果
     * @return TransactionResultDTO
     */
    protected TransactionResultDTO buildTransactionResult(Transaction transaction) {
        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(transaction.getOutTradeNo());
        transactionResult.setPayMethodTransactionId(transaction.getTransactionId());

        Transaction.TradeStateEnum tradeState = transaction.getTradeState();
        if (Transaction.TradeStateEnum.NOTPAY == tradeState) {
            transactionResult.setStatus(PayStatusEnum.NOT_PAY.getValue());
        } else if (Transaction.TradeStateEnum.SUCCESS == tradeState) {
            transactionResult.setStatus(PayStatusEnum.PAY_SUCCESS.getValue());
            transactionResult.setFinishTime(OffsetDateTime
                    .parse(transaction.getSuccessTime(), FORMATTER)
                    .toLocalDateTime());
        } else {
            transactionResult.setStatus(PayStatusEnum.PAY_CLOSED.getValue());
        }

        transactionResult.setNotifyResult("");
        return transactionResult;
    }

    @Override
    public void refund(RefundTransaction refundTransaction) {
        PayTransaction payTransaction = refundTransaction.getPayTransaction();
        String entrance = payTransaction.getEntrance();

        AmountReq amountReq = new AmountReq();
        amountReq.setRefund(refundTransaction.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
        amountReq.setTotal(payTransaction.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
        amountReq.setCurrency("CNY");

        CreateRequest createRequest = new CreateRequest();
        createRequest.setOutTradeNo(payTransaction.getTransactionId());
        createRequest.setOutRefundNo(refundTransaction.getTransactionId());
        createRequest.setReason(refundTransaction.getDescription());
        createRequest.setAmount(amountReq);
        createRequest.setNotifyUrl(notifyUrlProperties.getRefundNotifyUrl(entrance));

        try {
            WeChatPayFactory.getRefundService(weChatPayProperties, entrance).create(createRequest);
        } catch (Exception e) {
            throw new CustomizeException("微信支付 退款失败", e);
        }
    }

    @Override
    public TransactionResultDTO queryRefund(RefundTransaction refundTransaction) {
        PayTransaction payTransaction = refundTransaction.getPayTransaction();
        QueryByOutRefundNoRequest queryByOutRefundNoRequest = new QueryByOutRefundNoRequest();
        queryByOutRefundNoRequest.setOutRefundNo(refundTransaction.getTransactionId());

        try {
            Refund refund = WeChatPayFactory.getRefundService(weChatPayProperties, payTransaction.getEntrance())
                    .queryByOutRefundNo(queryByOutRefundNoRequest);

            return buildTransactionResult(refund);
        } catch (Exception e) {
            throw new CustomizeException("微信支付 查询退款失败", e);
        }
    }

    @Override
    public TransactionResultDTO parseRefundNotify(HttpServletRequest request, String entrance) {
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader(Constant.WECHAT_PAY_SERIAL))
                    .nonce(request.getHeader(Constant.WECHAT_PAY_NONCE))
                    .signature(request.getHeader(Constant.WECHAT_PAY_SIGNATURE))
                    .timestamp(request.getHeader(Constant.WECHAT_PAY_TIMESTAMP))
                    .body(ServletUtil.getBody(request))
                    .build();
            log.info("微信支付 退款通知参数：{}", requestParam);

            RefundNotification refundNotification = WeChatPayFactory
                    .getNotificationParser(weChatPayProperties, entrance)
                    .parse(requestParam, RefundNotification.class);
            log.info("微信支付 退款通知结果：{}", refundNotification);

            Refund refund = new Refund();
            refund.setOutRefundNo(refundNotification.getOutRefundNo());
            refund.setRefundId(refundNotification.getRefundId());
            refund.setStatus(refundNotification.getRefundStatus());
            refund.setSuccessTime(refundNotification.getSuccessTime());
            return buildTransactionResult(refund);
        } catch (Exception e) {
            throw new CustomizeException("微信支付 解析退款通知失败", e);
        }
    }

    /**
     * 构建交易结果
     *
     * @param refund 退款交易结果
     * @return TransactionResultDTO
     */
    private TransactionResultDTO buildTransactionResult(Refund refund) {
        TransactionResultDTO transactionResult = new TransactionResultDTO();
        transactionResult.setTransactionId(refund.getOutRefundNo());
        transactionResult.setPayMethodTransactionId(refund.getRefundId());

        Status status = refund.getStatus();
        if (Status.PROCESSING == status) {
            transactionResult.setStatus(RefundStatusEnum.IN_REFUND.getValue());
        } else if (Status.SUCCESS == status) {
            transactionResult.setStatus(RefundStatusEnum.REFUND_SUCCESS.getValue());
            transactionResult.setFinishTime(OffsetDateTime
                    .parse(refund.getSuccessTime(), FORMATTER)
                    .toLocalDateTime());
        } else {
            transactionResult.setStatus(RefundStatusEnum.REFUND_FAIL.getValue());
        }

        transactionResult.setNotifyResult("");
        return transactionResult;
    }
}
