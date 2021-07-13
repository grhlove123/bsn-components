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
        response = logRecord(methodKey, response);
        return super.decode(methodKey, response);
    }

    /**
     * 如果需要修改日志格式，可以覆盖该方法
     * note: 此处InternalUtil.acquireBody会读取body中的字节流并且关闭，但是会返回一个新的可读取response,
     * 因此向下传递时应该使用该新response
     * @param methodKey 方法标识
     * @param response 返回内容
     * @return 新response
     */
    public Response logRecord(String methodKey, Response response) {

        InternalUtil.InternalPair internalPair = InternalUtil.acquireBody(response);
        log.error("{} , url {} , headers {} , requestBody {} , status {} , reason {} , rawRes {}", methodKey, response.request().url(),
                InternalUtil.toJson(response.request().headers()), InternalUtil.getRawStr(response.request().body()),
                response.status(), response.reason(), InternalUtil.getRawStr(internalPair.getBodyData()));

        return internalPair.getResponse();
    }
}
