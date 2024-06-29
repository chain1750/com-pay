package com.chaincat.pay.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chaincat.pay.constant.RedisKeyConst;
import com.chaincat.pay.dao.PayTransactionDAO;
import com.chaincat.pay.dao.RefundTransactionDAO;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.enums.PayStatusEnum;
import com.chaincat.pay.enums.RefundStatusEnum;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 业务Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private PayTransactionDAO payTransactionDAO;

    @Autowired
    private RefundTransactionDAO refundTransactionDAO;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    @Autowired
    private GlobalPayMethodService payMethodService;

    @Autowired
    @Qualifier("notifyExecutor")
    private Executor notifyExecutor;

    @Override
    public String payNotify(HttpServletRequest request, String entrance) {
        // 解析支付通知
        TransactionResultDTO transactionResult = payMethodService.parsePayNotify(request, entrance);
        log.info("解析支付通知：{}", transactionResult);

        // 异步执行处理
        CompletableFuture.runAsync(() -> handlePayNotify(transactionResult), notifyExecutor)
                .exceptionally(e -> {
                    log.error("处理支付通知失败", e);
                    return null;
                });

        // 返回通知结果
        return transactionResult.getNotifyResult();
    }

    /**
     * 处理支付通知
     *
     * @param transactionResult 交易结果
     */
    private void handlePayNotify(TransactionResultDTO transactionResult) {
        String transactionId = transactionResult.getTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_PAY, transactionId);
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        PayTransaction payTransaction;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payTransaction = payTransactionDAO.getOne(Wrappers.<PayTransaction>lambdaQuery()
                        .eq(PayTransaction::getTransactionId, transactionId));
                Assert.notNull(payTransaction, "支付交易不存在");
                if (!PayStatusEnum.NOT_PAY.valueEquals(payTransaction.getStatus())) {
                    return;
                }
            } while (!locked);

            // 更新支付交易
            payTransaction.setPayMethodTransactionId(transactionResult.getPayMethodTransactionId());
            payTransaction.setStatus(transactionResult.getStatus());
            payTransaction.setFinishTime(transactionResult.getFinishTime());
            payTransactionDAO.updateById(payTransaction);

            // 通知业务方支付结果
            String msg = JSON.toJSONString(transactionResult);
            Message<String> message = MessageBuilder.withPayload(msg).build();
            rocketMqTemplate.send(payTransaction.getBizMqTopic(), message);
        } catch (InterruptedException e) {
            throw new CustomizeException("处理支付通知获取锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public String refundNotify(HttpServletRequest request, String entrance) {
        // 解析退款通知
        TransactionResultDTO transactionResult = payMethodService.parseRefundNotify(request, entrance);
        log.info("解析退款通知：{}", transactionResult);

        // 异步执行处理
        CompletableFuture.runAsync(() -> handleRefundNotify(transactionResult), notifyExecutor)
                .exceptionally(e -> {
                    log.error("处理退款通知失败", e);
                    return null;
                });

        // 返回通知数据
        return transactionResult.getNotifyResult();
    }

    /**
     * 处理退款通知
     *
     * @param transactionResult 交易结果
     */
    @SuppressWarnings("all")
    private void handleRefundNotify(TransactionResultDTO transactionResult) {
        String transactionId = transactionResult.getTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_REFUND_QUERY, transactionId);
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        RefundTransaction refundTransaction;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                refundTransaction = refundTransactionDAO.getOne(Wrappers.<RefundTransaction>lambdaQuery()
                        .eq(RefundTransaction::getTransactionId, transactionId));
                Assert.notNull(refundTransaction, "退款交易不存在");
                if (!RefundStatusEnum.IN_REFUND.valueEquals(refundTransaction.getStatus())) {
                    return;
                }
            } while (!locked);

            // 更新退款交易
            refundTransaction.setPayMethodTransactionId(transactionResult.getPayMethodTransactionId());
            refundTransaction.setStatus(transactionResult.getStatus());
            refundTransaction.setFinishTime(transactionResult.getFinishTime());
            refundTransactionDAO.updateById(refundTransaction);
        } catch (InterruptedException e) {
            throw new CustomizeException("处理退款通知获取锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 处理退款中任务
     *
     * @param refundTransaction 退款交易
     */
    @SuppressWarnings("all")
    private void handleInRefundTask(RefundTransaction refundTransaction) {
        String transactionId = refundTransaction.getTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_REFUND_QUERY, transactionId);
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                refundTransaction = refundTransactionDAO.getOne(Wrappers.<RefundTransaction>lambdaQuery()
                        .eq(RefundTransaction::getTransactionId, transactionId));
                Assert.notNull(refundTransaction, "退款交易不存在");
                if (!RefundStatusEnum.IN_REFUND.valueEquals(refundTransaction.getStatus())) {
                    return;
                }
            } while (!locked);

            // 查询支付交易
            PayTransaction payTransaction = payTransactionDAO.getOne(Wrappers.<PayTransaction>lambdaQuery()
                    .eq(PayTransaction::getTransactionId, refundTransaction.getPayTransactionId()));
            Assert.notNull(payTransaction, "支付交易不存在");

            // 查询退款
            refundTransaction.setPayTransaction(payTransaction);
            TransactionResultDTO transactionResult = payMethodService.queryRefund(refundTransaction);

            // 更新退款交易
            refundTransaction.setPayMethodTransactionId(transactionResult.getPayMethodTransactionId());
            refundTransaction.setStatus(transactionResult.getStatus());
            refundTransaction.setFinishTime(transactionResult.getFinishTime());
            refundTransactionDAO.updateById(refundTransaction);
        } catch (InterruptedException e) {
            throw new CustomizeException("处理退款任务获取锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
