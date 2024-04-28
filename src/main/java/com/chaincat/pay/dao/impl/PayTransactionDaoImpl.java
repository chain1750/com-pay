package com.chaincat.pay.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaincat.pay.dao.PayTransactionDAO;
import com.chaincat.pay.entity.PayTransaction;
import com.chaincat.pay.mapper.PayTransactionMapper;
import org.springframework.stereotype.Service;

/**
 * 支付交易DAO
 *
 * @author chenhaizhuang
 */
@Service
public class PayTransactionDaoImpl extends ServiceImpl<PayTransactionMapper, PayTransaction>
        implements PayTransactionDAO {
}
