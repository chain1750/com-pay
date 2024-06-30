package com.chaincat.pay.feign.bootuser.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 第三方用户OpenID获取请求
 *
 * @author chenhaizhuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
