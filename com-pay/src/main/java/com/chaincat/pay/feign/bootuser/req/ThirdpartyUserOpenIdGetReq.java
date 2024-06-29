package com.chaincat.pay.feign.bootuser.req;

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
     * 第三方类型
     */
    private Integer thirdpartyType;

    /**
     * 应用ID
     */
    private String appId;
}
