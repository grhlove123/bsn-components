package com.bsn.client.feign;

import com.bsn.client.utils.InternalUtil;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 解码异常时增加日志记录
 * @author liuyong4 2019/4/3 14:32
 **/
@Slf4j
public class LogErrorDecoder extends ErrorDecoder.Default{

    @Override
    public Exception decode(String methodKey, Response response) {
        InternalUtil.InternalPair internalPair = InternalUtil.acquireBody(response);
        log.error("{} url {} headers {} status {} reason {} rawRes {}", methodKey, response.request().url(), InternalUtil.toJson(response.request().headers()),
                response.status(), response.reason(), InternalUtil.getRawStr(internalPair.getBodyData()));
        return super.decode(methodKey, internalPair.getResponse());
    }
}
