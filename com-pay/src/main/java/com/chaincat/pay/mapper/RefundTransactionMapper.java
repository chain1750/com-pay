package com.chaincat.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaincat.pay.entity.RefundTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款交易Mapper
 *
 * @author chenhaizhuang
 */
@Mapper
public interface RefundTransactionMapper extends BaseMapper<RefundTransaction> {
}
