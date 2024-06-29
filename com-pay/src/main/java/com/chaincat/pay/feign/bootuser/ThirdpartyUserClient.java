package com.chaincat.pay.feign.bootuser;

import com.chaincat.pay.feign.bootuser.req.ThirdpartyUserOpenIdGetReq;
import com.chaincat.pay.feign.bootuser.resp.ThirdpartyUserOpenIdGetResp;
import com.chaincat.pay.model.IResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 第三方用户Client
 *
 * @author chenhaizhuang
 */
@FeignClient(name = "boot-user", path = "/boot/user/thirdpartyUser",
        contextId = "com.chaincat.pay.feign.bootuser.ThirdpartyUserClient")
public interface ThirdpartyUserClient {

    /**
     * 获取第三方用户OpenID
     *
     * @param req 请求
     * @return IResult
     */
    @GetMapping("/getThirdpartyUserOpenId")
    IResult<ThirdpartyUserOpenIdGetResp> getThirdpartyUserOpenId(ThirdpartyUserOpenIdGetReq req);
}
