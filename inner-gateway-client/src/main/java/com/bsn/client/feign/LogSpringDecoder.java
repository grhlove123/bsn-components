package com.bsn.client.feign;

import com.bsn.client.utils.InternalUtil;
import feign.Request;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

/**
 * 解码时记录日志
 * @author liuyong4 2019/4/3 14:34
 **/
@Slf4j
public class LogSpringDecoder implements Decoder {
    private final Decoder delegate;

    public LogSpringDecoder(Decoder delegate) {
        Objects.requireNonNull(delegate, "Decoder must not be null. ");
        this.delegate = delegate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (!isOptional(type)) {
            InternalUtil.InternalPair internalPair = InternalUtil.acquireBody(response);
            Object res = delegate.decode(internalPair.getResponse(), type);
            logRecord(response, InternalUtil.getRawStr(internalPair.getBodyData()), res);
            return res;
        }
        if (response.status() == 404 || response.status() == 204) {
            logRecord(response, "", "");
            return Optional.empty();
        }

        InternalUtil.InternalPair internalPair = InternalUtil.acquireBody(response);
        Type enclosedType = Util.resolveLastTypeParameter(type, Optional.class);
        Object res = delegate.decode(internalPair.getResponse(), enclosedType);
        logRecord(response, InternalUtil.getRawStr(internalPair.getBodyData()), res);
        return Optional.ofNullable(res);
    }

    private static boolean isOptional(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        return parameterizedType.getRawType().equals(Optional.class);
    }

    private void logRecord(Response response, String bodyData, Object res) {
        log.info("url {} , headers {} , body {} , status {} , reason {} , rawRes {} , convertRes {}", response.request().url(),
                InternalUtil.toJson(response.request().headers()), getBodyStr(response.request()),
                response.status(), response.reason(), bodyData, InternalUtil.toJson(res));
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
