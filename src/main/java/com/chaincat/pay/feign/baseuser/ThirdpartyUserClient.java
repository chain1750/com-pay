package com.chaincat.pay.feign.baseuser;

import com.chaincat.pay.feign.baseuser.req.ThirdpartyUserOpenIdGetReq;
import com.chaincat.pay.feign.baseuser.resp.ThirdpartyUserOpenIdGetResp;
import com.chaincat.pay.model.IResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 第三方用户Client
 *
 * @author chenhaizhuang
 */
@FeignClient(name = "base-user", path = "/base/user/user",
        contextId = "com.chaincat.pay.feign.baseuser.ThirdpartyUserClient")
public interface ThirdpartyUserClient {

    /**
     * 获取入口OpenID
     *
     * @param req 请求
     * @return IResult
     */
    @GetMapping("/getEntranceOpenId")
    IResult<ThirdpartyUserOpenIdGetResp> getEntranceOpenId(ThirdpartyUserOpenIdGetReq req);
}
