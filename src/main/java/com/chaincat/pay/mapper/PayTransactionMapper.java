package com.chaincat.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaincat.pay.entity.PayTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付交易Mapper
 *
 * @author chenhaizhuang
 */
@Mapper
public interface PayTransactionMapper extends BaseMapper<PayTransaction> {
}
