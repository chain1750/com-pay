package com.chaincat.pay.feign.bootuser.resp;

import lombok.Data;

/**
 * 第三方用户OpenID获取结果
 *
 * @author chenhaizhuang
 */
@Data
public class ThirdpartyUserOpenIdGetResp {

    /**
     * OpenID
     */
    private String openId;
}
