package com.bsn.client.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import feign.Response;
import feign.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static feign.Util.decodeOrDefault;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author liuyong4 2019/4/3 17:47
 **/
@Slf4j
public class InternalUtil {

    private static final ObjectMapper CAMEL_MAPPER = new ObjectMapper();

    static {
        CAMEL_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        CAMEL_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJson(Object object) {
        if (object instanceof String) {
            return (String) object;
        }

        try {
            return CAMEL_MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            log.info(" exception when toJson, object:{} ", object, e);
        }
        return "";
    }

    public static InternalPair acquireBody(Response response) {
        if (Objects.isNull(response)) {
            return new InternalPair(null, null);
        }

        byte[] bodyData = null;
        try {
            Map<String, Collection<String>> headers = handleContentType(response.headers());
            bodyData = Util.toByteArray(response.body().asInputStream());
            response = response.toBuilder().body(bodyData).headers(headers).build();
        } catch (IOException e) {
            log.error("LogSpringDecoder acquireBody acquire inputStream fail", e);
        }

        return new InternalPair(response, bodyData);
    }

    public static String getRawStr(byte[] bodyData) {
        String rawContent = "";
        if (Objects.nonNull(bodyData) && bodyData.length > 0) {
            rawContent = decodeOrDefault(bodyData, UTF_8, "");
        }
        return rawContent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternalPair {
        private Response response;
        private byte[] bodyData;
    }

    /**
     * 处理ContentType缺失/ContentType非json, 为了可以重用，因此设置为public
     * @param headers 原始header数据
     * @return 替换后的数据
     */
    public static Map<String, Collection<String>> handleContentType(Map<String, Collection<String>> headers) {
        Collection<String> contentType = headers.get(HttpHeaders.CONTENT_TYPE);
        boolean validJsonContentType = !CollectionUtils.isEmpty(contentType) &&
                (contentType.contains(MediaType.APPLICATION_JSON_VALUE) || contentType.contains(MediaType.APPLICATION_JSON_UTF8_VALUE));
        if (validJsonContentType) {
            return headers;
        }

        Map<String, Collection<String>> newHeaders = Maps.newHashMapWithExpectedSize(headers.size() + 1);
        newHeaders.putAll(headers);
        //防止存在旧值,且可能会存在全小写的key
        newHeaders.remove(HttpHeaders.CONTENT_TYPE);
        newHeaders.remove(HttpHeaders.CONTENT_TYPE.toLowerCase());
        newHeaders.put(HttpHeaders.CONTENT_TYPE, Lists.newArrayList(MediaType.APPLICATION_JSON_VALUE));
        return newHeaders;
    }
}
