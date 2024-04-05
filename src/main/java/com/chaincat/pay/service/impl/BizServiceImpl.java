package com.chaincat.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private StrategySelector strategySelector;

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
}
