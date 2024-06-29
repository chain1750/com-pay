package com.chaincat.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.resp.QueryPayResp;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.service.BizService;
import com.chaincat.pay.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
public class ScheduleServiceImpl implements ScheduleService {

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
    private BizService bizService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNotPay() {
        // 查询未支付的交易
        List<PayTransaction> payTransactions = payTransactionDAO.list(Wrappers.<PayTransaction>lambdaQuery()
                .select(PayTransaction::getTransactionId)
                .eq(PayTransaction::getStatus, PayStatusEnum.NOT_PAY.getValue()));
        log.info("处理未支付数量：{}", payTransactions.size());

        // 异步执行处理
        payTransactions.forEach(payTransaction -> CompletableFuture.runAsync(() -> {
                            QueryPayReq req = new QueryPayReq();
                            req.setTransactionId(payTransaction.getTransactionId());
                            QueryPayResp queryPayResp = bizService.queryPay(req);

                            // 通知业务方支付结果
                            String msg = JSON.toJSONString(BeanUtil.copyProperties(queryPayResp, TransactionResultDTO.class));
                            Message<String> message = MessageBuilder.withPayload(msg).build();
                            rocketMqTemplate.send(payTransaction.getBizMqTopic(), message);
                        }, taskExecutor)
                        .exceptionally(e -> {
                            log.error("处理未支付失败", e);
                            return null;
                        })
        );
    }

    @Override
    public void handleInRefund() {
        // 查询退款中的交易
        List<RefundTransaction> refundTransactions = refundTransactionDAO.list(Wrappers.<RefundTransaction>lambdaQuery()
                .select(RefundTransaction::getTransactionId)
                .eq(RefundTransaction::getStatus, RefundStatusEnum.IN_REFUND.getValue()));
        log.info("处理退款中数量：{}", refundTransactions.size());

        // 异步执行处理
        refundTransactions.forEach(refundTransaction ->
                CompletableFuture.runAsync(() -> handleInRefundTask(refundTransaction), taskExecutor)
                        .exceptionally(e -> {
                            log.error("处理退款中失败", e);
                            return null;
                        })
        );
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
