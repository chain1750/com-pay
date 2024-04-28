package com.chaincat.pay.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaincat.pay.dao.RefundTransactionDAO;
import com.chaincat.pay.entity.RefundTransaction;
import com.chaincat.pay.mapper.RefundTransactionMapper;
import org.springframework.stereotype.Service;

/**
 * 退款交易DAO
 *
 * @author chenhaizhuang
 */
@Service
public class RefundTransactionDaoImpl extends ServiceImpl<RefundTransactionMapper, RefundTransaction>
        implements RefundTransactionDAO {
}
