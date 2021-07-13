package com.bsn.client.feign;

import com.bsn.client.properties.ProxyProperties;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 兼容proxy服务，在请求正式请求之前增加修改相关的内容
 * 代码参考服务后台 CustomHttpClient
 * @author liuyong4 2019/4/10 11:42
 **/
@Slf4j
public class ProxyHttpClient implements Client  {
    private static final String REQUEST_ID_KEY = "REQUEST_ID";
    private static final String PROXY_HEADER = "target-domain";
    private static final String REQUEST_ID_HEADER = "x-request-id";
    private static final String CLIENT_IP_SOURCE_HEADER = "X-Real-IP";
    private static final String CLIENT_IP_TARGET_HEADER = "x-client-ip";
    private static final Set<String> ADD_USER_IP_DOMAINS = new HashSet<>();

    static {
        ADD_USER_IP_DOMAINS.add("us.speiyou.cn");
    }

    private ApacheHttpClient apacheHttpClient;

    private ProxyProperties proxyProperties;

    public ProxyHttpClient(HttpClient httpClient, ProxyProperties proxyProperties) {
        this.apacheHttpClient = new ApacheHttpClient(httpClient);
        this.proxyProperties = proxyProperties;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        try {
            //是否直接通过指定的url调用,而非服务发现
            Boolean directCall = !MarkedLoadBalancerFeignClient.LOAD_BALANCE_FLAG.get();
            if (directCall && proxyProperties.getEnable()) {
                request = rewriteToProxy(request);
            }
            return apacheHttpClient.execute(request, options);
        } finally {
            MarkedLoadBalancerFeignClient.LOAD_BALANCE_FLAG.remove();
        }
    }

    private Request rewriteToProxy(Request request) {
        try {
            URI uri = new URI(request.url());
            return rewriteToProxyIfNotIgnored(uri, request);
        } catch (URISyntaxException e) {
            log.info("ProxyHttpClient | rewriteToProxy parse url fail {} ", request.url(), e);
            return request;
        }
    }

    private Request rewriteToProxyIfNotIgnored(URI uri, Request request) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String targetDomain = scheme + "://" + host;
        Map<String, Collection<String>> newHeader = Maps.newHashMapWithExpectedSize(request.headers().size() + 3);
        newHeader.putAll(request.headers());

        addExtraHeader(host, request, newHeader);
        if (proxyProperties.getIgnoreDomains().contains(targetDomain)) {
            return request;
        }
        if (uri.getPort() != -1) {
            targetDomain = targetDomain + ":" + uri.getPort();
        }
        newHeader.put(PROXY_HEADER, Lists.newArrayList(targetDomain));
        addTraceHeader(request, newHeader);
        addOriginHeader(request, newHeader);

        String proxyUrl = uri.toString().replace(targetDomain, proxyProperties.getBaseURL());
        return Request.create(request.method(), proxyUrl, newHeader, request.body(), request.charset());
    }

    private void addTraceHeader(Request request, Map<String, Collection<String>> newHeader) {
        String requestId = MDC.get(REQUEST_ID_KEY);
        if (StringUtils.isNotBlank(requestId)) {
            newHeader.put(REQUEST_ID_HEADER, Lists.newArrayList(proxyProperties.getTracePrefix() + "-" + requestId));
        }
    }

    private void addOriginHeader(Request request, Map<String, Collection<String>> newHeader) {
        if (CollectionUtils.isEmpty(proxyProperties.getOriginUrls())) {
            return;
        }

        String url = request.url();
        final String xesOrigin = proxyProperties.getXesOrigin();
        proxyProperties.getOriginUrls().stream().filter(url::contains).findFirst().ifPresent(ignore -> {
            newHeader.put("xes-origin", Lists.newArrayList(xesOrigin));
        });
    }

    private void addExtraHeader(String originDomain, Request request, Map<String, Collection<String>> newHeader) {
        if (ADD_USER_IP_DOMAINS.contains(originDomain)) {
            Optional.ofNullable(request)
                    .map(Request::headers)
                    .map(header -> header.get(CLIENT_IP_SOURCE_HEADER))
                    .filter(item -> !CollectionUtils.isEmpty(item))
                    .ifPresent(ip -> newHeader.put(CLIENT_IP_TARGET_HEADER, ip));
        }
    }
}