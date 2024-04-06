package com.chaincat.pay.feign.baseuser.req;

import lombok.Data;

/**
 * 第三方用户OpenID获取请求
 *
 * @author chenhaizhuang
 */
@Data
public class ThirdpartyUserOpenIdGetReq {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private String appId;
}
