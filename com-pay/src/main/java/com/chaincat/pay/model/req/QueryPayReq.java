package com.chaincat.pay.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 查询支付
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryPayReq {

    /**
     * 交易ID，交易的唯一标识
     */
    @NotBlank(message = "交易ID不能为空")
    private String transactionId;
}
