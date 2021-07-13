package com.bsn.client.feign;

import feign.Response;
import org.slf4j.Logger;

/**
 * feign decoder 日志模板
 * @author liuyong4 2019/4/10 15:32
 **/
public interface FeignDecoderLogTemplate {

    /**
     * 日志模板，实现该interface，并且注入之后可以修改logSpringDecoder中的日志格式
     * @param log logger实例
     * @param response http返回结果
     * @param bodyData http请求返回后接受到的原始字符信息
     * @param res 反序列化之后的对象
     */
    void logRecord(Logger log, Response response, String bodyData, Object res);
}
