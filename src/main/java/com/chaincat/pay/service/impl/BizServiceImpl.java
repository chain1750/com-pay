package com.chaincat.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chaincat.pay.constant.RedisKeyConst;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.enums.PayStatusEnum;
import com.chaincat.pay.enums.RefundStatusEnum;
import com.chaincat.pay.exception.CustomizeException;
import com.chaincat.pay.mapper.PayTransactionMapper;
import com.chaincat.pay.mapper.RefundTransactionMapper;
import com.chaincat.pay.model.dto.TransactionResultDTO;
import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.QueryPayResp;
import com.chaincat.pay.model.resp.RefundResp;
import com.chaincat.pay.service.BizService;
import com.chaincat.pay.strategy.StrategySelector;
import com.chaincat.pay.utils.IdUtils;
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

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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
public class BizServiceImpl implements BizService {

    @Autowired
    private PayTransactionMapper payTransactionMapper;

    @Autowired
    private RefundTransactionMapper refundTransactionMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    @Autowired
    private StrategySelector strategySelector;

    @Autowired
    @Qualifier("notifyExecutor")
    private Executor notifyExecutor;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrepayResp prepay(PrepayReq req) {
        String key = StrUtil.format(RedisKeyConst.LOCK_PREPAY, req.getBiz(), req.getBizDataId());
        RLock lock = redissonClient.getLock(key);
        Assert.isTrue(lock.tryLock(), "操作频繁，请稍后再试");
        try {
            LocalDateTime now = LocalDateTime.now();
            Assert.isTrue(now.isBefore(req.getExpireTime()), "支付过期时间必须大于当前时间");

            // 创建支付交易
            PayTransaction payTransaction = BeanUtil.copyProperties(req, PayTransaction.class);
            payTransaction.setTransactionId(IdUtils.generateTransactionId("10001", now));
            payTransaction.setStatus(PayStatusEnum.NOT_PAY.getValue());
            payTransactionMapper.insert(payTransaction);
            log.info("创建支付交易：{}", payTransaction);

            // 预支付
            String prepay = strategySelector.prepay(payTransaction, payTransaction.getEntrance());
            log.info("预支付：{}", prepay);

            // 预支付结果
            PrepayResp resp = new PrepayResp();
            resp.setTransactionId(payTransaction.getTransactionId());
            resp.setPrepay(prepay);
            return resp;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePay(ClosePayReq req) {
        String transactionId = req.getTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_PAY, transactionId);
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        PayTransaction payTransaction;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payTransaction = payTransactionMapper.selectOne(Wrappers.<PayTransaction>lambdaQuery()
                        .eq(PayTransaction::getTransactionId, transactionId));
                Assert.notNull(payTransaction, "支付交易不存在");
                Assert.isTrue(PayStatusEnum.NOT_PAY.valueEquals(payTransaction.getStatus()),
                        "交易已支付/已关闭，无法关闭");
            } while (!locked);

            // 更新支付交易
            payTransaction.setStatus(PayStatusEnum.PAY_CLOSED.getValue());
            payTransactionMapper.updateById(payTransaction);
            log.info("关闭支付更新支付交易：{}", payTransaction);

            // 关闭支付
            strategySelector.closePay(payTransaction, payTransaction.getEntrance());
        } catch (InterruptedException e) {
            throw new CustomizeException("关闭支付获取锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueryPayResp queryPay(QueryPayReq req) {
        String transactionId = req.getTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_PAY, transactionId);
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        PayTransaction payTransaction;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payTransaction = payTransactionMapper.selectOne(Wrappers.<PayTransaction>lambdaQuery()
                        .eq(PayTransaction::getTransactionId, transactionId));
                Assert.notNull(payTransaction, "支付交易不存在");
                // 已支付/已关闭返回支付结果
                if (!PayStatusEnum.NOT_PAY.valueEquals(payTransaction.getStatus())) {
                    return BeanUtil.copyProperties(payTransaction, QueryPayResp.class);
                }
            } while (!locked);

            // 查询支付
            TransactionResultDTO transactionResult = strategySelector
                    .queryPay(payTransaction, payTransaction.getEntrance());
            log.info("查询支付：{}", transactionResult);

            // 更新
            updatePay(transactionResult, payTransaction);

            // 返回支付结果
            return BeanUtil.copyProperties(payTransaction, QueryPayResp.class);
        } catch (InterruptedException e) {
            throw new CustomizeException("查询支付获取锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 更新支付交易
     *
     * @param transactionResult 交易结果
     * @param payTransaction    支付交易
     */
    private void updatePay(TransactionResultDTO transactionResult, PayTransaction payTransaction) {
        // 过期关闭支付
        Integer status = transactionResult.getStatus();
        if (PayStatusEnum.NOT_PAY.valueEquals(status) && LocalDateTime.now().isAfter(payTransaction.getExpireTime())) {
            log.info("支付交易过期，关闭支付：{}", payTransaction.getTransactionId());
            strategySelector.closePay(payTransaction, payTransaction.getEntrance());
            transactionResult.setStatus(PayStatusEnum.PAY_CLOSED.getValue());
        }

        // 未支付不更新
        status = transactionResult.getStatus();
        if (PayStatusEnum.NOT_PAY.valueEquals(status)) {
            return;
        }

        // 更新支付交易
        payTransaction.setPayMethodTransactionId(transactionResult.getPayMethodTransactionId());
        payTransaction.setStatus(status);
        payTransaction.setFinishTime(transactionResult.getFinishTime());
        payTransactionMapper.updateById(payTransaction);
    }

    @Override
    public String payNotify(HttpServletRequest request, String entrance) {
        // 解析支付通知
        TransactionResultDTO transactionResult = strategySelector.parsePayNotify(request, entrance);
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
                payTransaction = payTransactionMapper.selectOne(Wrappers.<PayTransaction>lambdaQuery()
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
            payTransactionMapper.updateById(payTransaction);

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
    @Transactional(rollbackFor = Exception.class)
    public void handleNotPay() {
        // 查询未支付的交易
        List<PayTransaction> payTransactions = payTransactionMapper.selectList(Wrappers.<PayTransaction>lambdaQuery()
                .select(PayTransaction::getTransactionId)
                .eq(PayTransaction::getStatus, PayStatusEnum.NOT_PAY.getValue()));
        log.info("处理未支付数量：{}", payTransactions.size());

        // 异步执行处理
        payTransactions.forEach(payTransaction -> CompletableFuture.runAsync(() -> {
                            QueryPayReq req = new QueryPayReq();
                            req.setTransactionId(payTransaction.getTransactionId());
                            QueryPayResp queryPayResp = queryPay(req);

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
    @Transactional(rollbackFor = Exception.class)
    public RefundResp refund(RefundReq req) {
        String payTransactionId = req.getPayTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_REFUND_EXEC, payTransactionId);
        RLock lock = redissonClient.getLock(key);
        Assert.isTrue(lock.tryLock(), "操作频繁，请稍后再试");
        try {
            PayTransaction payTransaction = payTransactionMapper.selectOne(Wrappers.<PayTransaction>lambdaQuery()
                    .eq(PayTransaction::getTransactionId, payTransactionId));
            Assert.notNull(payTransaction, "支付交易不存在");
            Assert.isTrue(PayStatusEnum.PAY_SUCCESS.valueEquals(payTransaction.getStatus()), "交易未支付，无法退款");

            // 创建退款交易
            LocalDateTime now = LocalDateTime.now();
            RefundTransaction refundTransaction = BeanUtil.copyProperties(req, RefundTransaction.class);
            refundTransaction.setTransactionId(IdUtils.generateTransactionId("10002", now));
            refundTransaction.setStatus(RefundStatusEnum.IN_REFUND.getValue());
            refundTransactionMapper.insert(refundTransaction);
            log.info("创建退款交易：{}", refundTransaction);

            // 退款
            refundTransaction.setPayTransaction(payTransaction);
            strategySelector.refund(refundTransaction, payTransaction.getEntrance());

            // 退款交易ID
            RefundResp resp = new RefundResp();
            resp.setTransactionId(refundTransaction.getTransactionId());
            return resp;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String refundNotify(HttpServletRequest request, String entrance) {
        // 解析退款通知
        TransactionResultDTO transactionResult = strategySelector.parseRefundNotify(request, entrance);
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
                refundTransaction = refundTransactionMapper.selectOne(Wrappers.<RefundTransaction>lambdaQuery()
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
            refundTransactionMapper.updateById(refundTransaction);
        } catch (InterruptedException e) {
            throw new CustomizeException("处理退款通知获取锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public void handleInRefund() {
        // 查询退款中的交易
        List<RefundTransaction> refundTransactions = refundTransactionMapper
                .selectList(Wrappers.<RefundTransaction>lambdaQuery()
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
    private void handleInRefundTask(RefundTransaction refundTransaction) {
        String transactionId = refundTransaction.getTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_REFUND_QUERY, transactionId);
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                refundTransaction = refundTransactionMapper.selectOne(Wrappers.<RefundTransaction>lambdaQuery()
                        .eq(RefundTransaction::getTransactionId, transactionId));
                Assert.notNull(refundTransaction, "退款交易不存在");
                if (!RefundStatusEnum.IN_REFUND.valueEquals(refundTransaction.getStatus())) {
                    return;
                }
            } while (!locked);

            // 查询支付交易
            PayTransaction payTransaction = payTransactionMapper.selectOne(Wrappers.<PayTransaction>lambdaQuery()
                    .eq(PayTransaction::getTransactionId, refundTransaction.getPayTransactionId()));
            Assert.notNull(payTransaction, "支付交易不存在");

            // 查询退款
            refundTransaction.setPayTransaction(payTransaction);
            TransactionResultDTO transactionResult = strategySelector
                    .queryRefund(refundTransaction, payTransaction.getEntrance());

            // 更新退款交易
            refundTransaction.setPayMethodTransactionId(transactionResult.getPayMethodTransactionId());
            refundTransaction.setStatus(transactionResult.getStatus());
            refundTransaction.setFinishTime(transactionResult.getFinishTime());
            refundTransactionMapper.updateById(refundTransaction);
        } catch (InterruptedException e) {
            throw new CustomizeException("处理退款任务获取锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
