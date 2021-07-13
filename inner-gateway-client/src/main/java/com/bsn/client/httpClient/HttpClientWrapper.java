package com.bsn.client.httpClient;


import com.bsn.client.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * http 请求工具类
 *
 * @author guoronghua 2020/7/3 下午8:45
 */
@Slf4j
public class HttpClientWrapper {

    private HttpClient httpClient;

    public HttpClientWrapper(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    protected String post(Map<String, String> headerMap, String url, List<NameValuePair> formParam) throws IOException {
        HttpClientResponse response = postReturnHttpResponse(headerMap, url, formParam);
        String entityStr = response.getResponseBody();
        return entityStr;
    }

    protected HttpClientResponse postReturnHttpResponse(Map<String, String> headerMap, String url, List<NameValuePair> formParam)
            throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (headerMap != null) {
            headerMap.forEach(httpPost::addHeader);
        }
        httpPost.setEntity(new UrlEncodedFormEntity(formParam, "UTF-8"));
        final HttpClientResponse httpClientResponse = convert2HttpClientResponse(httpClient.execute(httpPost));
        log.info("postReturnHttpResponse | url is {},header:{}, request:{}, response:{}", url, JsonUtils.serializeToJson(headerMap), JsonUtils.serializeToJson(formParam), JsonUtils.serializeToJson(httpClientResponse));
        return httpClientResponse;
    }

    protected HttpClientResponse postReturnHttpResponse(Map<String, String> headerMap, String url, Map<String, String[]> queryParams) throws IOException {
        String newUrl = addQueryParamsToUrl(url, queryParams);
        HttpPost httpPost = new HttpPost(newUrl);
        if (headerMap != null) {
            headerMap.forEach(httpPost::addHeader);
        }
        httpPost.setEntity(null);
        HttpClientResponse httpClientResponse = convert2HttpClientResponse(httpClient.execute(httpPost));
        log.info("postReturnHttpResponse | url is {},header:{}, request:{}, response:{}", url, JsonUtils.serializeToJson(headerMap), JsonUtils.serializeToJson(queryParams), JsonUtils.serializeToJson(httpClientResponse));
        return httpClientResponse;
    }

    protected HttpClientResponse postReturnHttp2Response(Map<String, String> headerMap, String url, Map<String, String> queryParams) throws IOException {
        String newUrl = addQueryParams2Url(url, queryParams);
        HttpPost httpPost = new HttpPost(newUrl);
        if (headerMap != null) {
            headerMap.forEach(httpPost::addHeader);
        }
        httpPost.setEntity(null);
        final HttpClientResponse httpClientResponse = convert2HttpClientResponse(httpClient.execute(httpPost));
        log.info("postReturnHttpResponse | url is {},header:{}, request:{}, response:{}", url, JsonUtils.serializeToJson(headerMap), JsonUtils.serializeToJson(queryParams), JsonUtils.serializeToJson(httpClientResponse));
        return httpClientResponse;
    }

    protected HttpClientResponse postReturnHttpResponse(Map<String, String> headerMap, String url, String bodyStr) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (headerMap != null) {
            headerMap.forEach(httpPost::addHeader);
        }
        httpPost.setEntity(new StringEntity(bodyStr, "UTF-8"));
        final HttpClientResponse httpClientResponse = convert2HttpClientResponse(httpClient.execute(httpPost));
        log.info("postReturnHttpResponse | url is {},header:{}, request:{}, response:{}", url, JsonUtils.serializeToJson(headerMap), bodyStr, JsonUtils.serializeToJson(httpClientResponse));
        return httpClientResponse;
    }

    public HttpClientResponse get(Map<String, String> headers, String url, Map<String, String[]> queryParams) throws IOException {
        String newUrl = addQueryParamsToUrl(url, queryParams);
        return get(headers, newUrl);
    }


    protected HttpClientResponse getWithEncoded(Map<String, String> headers, String url, Map<String, String[]> queryParams) throws IOException, URISyntaxException {

        List<NameValuePair> nameValuePairs = queryParams.entrySet()
                .stream()
                .flatMap(e -> Arrays.stream(e.getValue()).map(p -> new BasicNameValuePair(e.getKey(), p)))
                .collect(Collectors.toList());

        String newUrl = new URIBuilder(url).addParameters(nameValuePairs).toString();

        return get(headers, newUrl);
    }


    public HttpClientResponse get(Map<String, String> headers, String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        if (null != headers) {
            headers.forEach(httpGet::addHeader);
        }
        final HttpClientResponse httpClientResponse = convert2HttpClientResponse(httpClient.execute(httpGet));
        log.info("get | header:{}, url:{}, response:{}", JsonUtils.serializeToJson(headers), url, JsonUtils.serializeToJson(httpClientResponse));
        return httpClientResponse;
    }

    protected HttpClientResponse get(String url, Map<String, String[]> queryParams) throws IOException {
        String newUrl = addQueryParamsToUrl(url, queryParams);
        return get(null, newUrl);
    }

    protected HttpClientResponse get(String url, Map<String, String> headers, Map<String, String> queryParams) throws IOException {
        String newUrl = addQueryParams2Url(url, queryParams);
        return get(headers, newUrl);
    }

    private String addQueryParamsToUrl(String url, Map<String, String[]> queryParams) {
        StringBuilder sb = new StringBuilder();
        if (null != queryParams && !queryParams.isEmpty()) {
            sb.append("?");
            queryParams.entrySet().forEach(param -> {
                String key = param.getKey();
                Arrays.stream(param.getValue()).filter(StringUtils::isNotBlank)
                        .forEach(value -> sb.append(key).append("=").append(value).append("&"));
            });
            int length = sb.length() - 1;
            if (sb.indexOf("&", length) >= 0) {
                sb.deleteCharAt(length);
            } else if (length == 0) {
                sb.deleteCharAt(0);
            }
        }
        return url + sb.toString();
    }

    private String addQueryParams2Url(String url, Map<String, String> queryParams) {
        if (CollectionUtils.isEmpty(queryParams)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        sb.append(url).append("?");
        queryParams.entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue())).forEach(s -> {
            sb.append("&").append(s.getKey()).append("=").append(s.getValue());
        });
        return sb.toString().replaceFirst("&", "");
    }

    private HttpClientResponse convert2HttpClientResponse(HttpResponse httpResponse) throws IOException {
        String responseBody = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        return new HttpClientResponse(httpResponse.getStatusLine().getStatusCode(), responseBody);
    }

    protected HttpClientResponse postJSONObject(Map<String, String> headerMap, String url, String bodyStr) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (headerMap != null) {
            headerMap.forEach(httpPost::addHeader);
        }
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(bodyStr, "UTF-8"));
        final HttpClientResponse httpClientResponse = convert2HttpClientResponse(httpClient.execute(httpPost));
        log.info("postReturnHttpResponse | url is {},header:{}, request:{}, response:{}", url, JsonUtils.serializeToJson(headerMap), bodyStr, JsonUtils.serializeToJson(httpClientResponse));
        return httpClientResponse;
    }

}
