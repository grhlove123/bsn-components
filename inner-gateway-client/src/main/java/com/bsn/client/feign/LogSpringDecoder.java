package com.bsn.client.feign;

import com.bsn.client.utils.InternalUtil;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 解码时记录日志
 * @author liuyong4 2019/4/3 14:34
 **/
@Slf4j
public class LogSpringDecoder extends ResponseEntityDecoder {

    private FeignDecoderLogTemplate feignDecoderLogTemplate;

    public LogSpringDecoder(Decoder decoder) {
        super(decoder);
    }

    public LogSpringDecoder(Decoder decoder, FeignDecoderLogTemplate feignDecoderLogTemplate) {
        super(decoder);
        this.feignDecoderLogTemplate = feignDecoderLogTemplate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        InternalUtil.InternalPair internalPair = InternalUtil.acquireBody(response);
        Object res = super.decode(internalPair.getResponse(), type);
        logRecord(internalPair.getResponse(), InternalUtil.getRawStr(internalPair.getBodyData()), res);
        return res;
    }

    private void logRecord(Response response, String bodyData, Object res) {
        if (Objects.isNull(feignDecoderLogTemplate)) {
            log.info("url {} , headers {} , body {} , status {} , reason {} , rawRes {} , convertRes {}", response.request().url(),
                    InternalUtil.toJson(response.request().headers()), getBodyStr(response.request()),
                    response.status(), response.reason(), bodyData, InternalUtil.toJson(res));
        } else {
            feignDecoderLogTemplate.logRecord(log, response, bodyData, res);
        }
    }

    private static String getBodyStr(Request request) {
        if (Objects.isNull(request)) {
            return "";
        }

        byte[] data = request.body();
        Charset encoding = request.charset();
        return Objects.nonNull(encoding) && Objects.nonNull(data)
                ? new String(data, encoding)
                : "";
    }
}
