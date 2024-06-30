package com.chaincat.pay.feign.bootuser.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 第三方用户OpenID获取结果
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdpartyUserOpenIdGetResp {

    /**
     * OpenID
     */
    private String openId;
}
