package com.chaincat.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.QueryPayResp;
import com.chaincat.pay.model.resp.RefundResp;
import com.chaincat.pay.paymethod.GlobalPayMethodService;
import com.chaincat.pay.service.BizService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private PayTransactionDAO payTransactionDAO;

    @Autowired
    private RefundTransactionDAO refundTransactionDAO;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GlobalPayMethodService payMethodService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrepayResp prepay(PrepayReq req) {
        String key = StrUtil.format(RedisKeyConst.LOCK_PREPAY, req.getBiz(), req.getBizDataId());
        RLock lock = redissonClient.getLock(key);
        Assert.isTrue(lock.tryLock(), "操作频繁，请稍后再试");
        try {
            // 关闭未支付
            closeNotPay(req);

            LocalDateTime now = LocalDateTime.now();
            Assert.isTrue(now.isBefore(req.getExpireTime()), "支付过期时间必须大于当前时间");

            String id = "10001" + now.format(DatePattern.PURE_DATE_FORMATTER) + IdUtil.getSnowflakeNextIdStr();

            // 创建支付交易
            PayTransaction payTransaction = BeanUtil.copyProperties(req, PayTransaction.class);
            payTransaction.setTransactionId(id);
            payTransaction.setStatus(PayStatusEnum.NOT_PAY.getValue());
            payTransactionDAO.save(payTransaction);
            log.info("创建支付交易：{}", payTransaction);

            // 预支付
            String prepay = payMethodService.prepay(payTransaction);
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

    @Transactional(rollbackFor = Exception.class)
    public void closeNotPay(PrepayReq req) {
        // 查询该业务下的交易
        List<PayTransaction> payTransactions = payTransactionDAO.list(Wrappers.<PayTransaction>lambdaQuery()
                .eq(PayTransaction::getBiz, req.getBiz())
                .eq(PayTransaction::getBizDataId, req.getBizDataId()));
        if (CollUtil.isEmpty(payTransactions)) {
            return;
        }
        /*
        如果存在已支付，则报错
        如果存在未支付，则关闭该支付
         */
        for (PayTransaction payTransaction : payTransactions) {
            Assert.isTrue(!PayStatusEnum.PAY_SUCCESS.valueEquals(payTransaction.getStatus()), "业务已完成支付");
            if (PayStatusEnum.PAY_CLOSED.valueEquals(payTransaction.getStatus())) {
                continue;
            }
            ClosePayReq closePayReq = new ClosePayReq();
            closePayReq.setTransactionId(payTransaction.getTransactionId());
            closePay(closePayReq);
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
                payTransaction = payTransactionDAO.getOne(Wrappers.<PayTransaction>lambdaQuery()
                        .eq(PayTransaction::getTransactionId, transactionId));
                Assert.notNull(payTransaction, "支付交易不存在");
                Assert.isTrue(PayStatusEnum.NOT_PAY.valueEquals(payTransaction.getStatus()),
                        "交易已支付/已关闭，无法关闭");
            } while (!locked);

            // 更新支付交易
            payTransaction.setStatus(PayStatusEnum.PAY_CLOSED.getValue());
            payTransactionDAO.updateById(payTransaction);
            log.info("关闭支付更新支付交易：{}", payTransaction);

            // 关闭支付
            payMethodService.closePay(payTransaction);
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
                payTransaction = payTransactionDAO.getOne(Wrappers.<PayTransaction>lambdaQuery()
                        .eq(PayTransaction::getTransactionId, transactionId));
                Assert.notNull(payTransaction, "支付交易不存在");
                // 已支付/已关闭返回支付结果
                if (!PayStatusEnum.NOT_PAY.valueEquals(payTransaction.getStatus())) {
                    return BeanUtil.copyProperties(payTransaction, QueryPayResp.class);
                }
            } while (!locked);

            // 查询支付
            TransactionResultDTO transactionResult = payMethodService.queryPay(payTransaction);
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
            payMethodService.closePay(payTransaction);
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
        payTransactionDAO.updateById(payTransaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResp refund(RefundReq req) {
        String payTransactionId = req.getPayTransactionId();
        String key = StrUtil.format(RedisKeyConst.LOCK_REFUND_EXEC, payTransactionId);
        RLock lock = redissonClient.getLock(key);
        Assert.isTrue(lock.tryLock(), "操作频繁，请稍后再试");
        try {
            PayTransaction payTransaction = payTransactionDAO.getOne(Wrappers.<PayTransaction>lambdaQuery()
                    .eq(PayTransaction::getTransactionId, payTransactionId));
            Assert.notNull(payTransaction, "支付交易不存在");
            Assert.isTrue(PayStatusEnum.PAY_SUCCESS.valueEquals(payTransaction.getStatus()), "交易未支付，无法退款");

            // 创建退款交易
            LocalDateTime now = LocalDateTime.now();
            String id = "10002" + now.format(DatePattern.PURE_DATE_FORMATTER) + IdUtil.getSnowflakeNextIdStr();
            RefundTransaction refundTransaction = BeanUtil.copyProperties(req, RefundTransaction.class);
            refundTransaction.setTransactionId(id);
            refundTransaction.setStatus(RefundStatusEnum.IN_REFUND.getValue());
            refundTransactionDAO.save(refundTransaction);
            log.info("创建退款交易：{}", refundTransaction);

            // 退款
            refundTransaction.setPayTransaction(payTransaction);
            payMethodService.refund(refundTransaction);

            // 退款交易ID
            RefundResp resp = new RefundResp();
            resp.setTransactionId(refundTransaction.getTransactionId());
            return resp;
        } finally {
            lock.unlock();
        }
    }
}
