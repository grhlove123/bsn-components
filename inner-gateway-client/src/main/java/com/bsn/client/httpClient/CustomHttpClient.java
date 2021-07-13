package com.bsn.client.httpClient;

import com.bsn.client.properties.ProxyProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;

/**
* @author guoronghua 2021/6/23 9:06 PM
*/
@Slf4j
public class CustomHttpClient implements HttpClient {

    private static final String PROXY_HEADER = "target-domain";
    private static final String GW_FROM_APP = "request-source";
    private HttpClient delegate;
    private ProxyProperties proxyProperties;


    public CustomHttpClient(HttpClient delegate, ProxyProperties proxyProperties) {
        this.delegate = delegate;
        this.proxyProperties = proxyProperties;
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
        return delegate.getParams();
    }

    @Override
    @Deprecated
    public ClientConnectionManager getConnectionManager() {
        return delegate.getConnectionManager();
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException {
        rewriteToProxyIfNotIgnored((HttpRequestBase) request);
        return delegate.execute(request);
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
        rewriteToProxyIfNotIgnored((HttpRequestBase) request);
        return delegate.execute(request, context);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
        HttpHost proxyTarget = rewriteToProxyIfNotIgnored(target, request);
        return delegate.execute(proxyTarget, request);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        HttpHost proxyTarget = rewriteToProxyIfNotIgnored(target, request);
        return delegate.execute(proxyTarget, request, context);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
        rewriteToProxyIfNotIgnored((HttpRequestBase) request);
        return delegate.execute(request, responseHandler);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
        rewriteToProxyIfNotIgnored((HttpRequestBase) request);
        return delegate.execute(request, responseHandler, context);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpHost proxyTarget = rewriteToProxyIfNotIgnored(target, request);
        return delegate.execute(proxyTarget, request, responseHandler);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
        rewriteToProxyIfNotIgnored((HttpRequestBase) request);
        return delegate.execute(target, request, responseHandler, context);
    }


    private HttpHost rewriteToProxyIfNotIgnored(HttpHost target, HttpRequest request) {
        if (!proxyProperties.getEnable()) {
            log.info("rewriteToProxyIfNotIgnored | proxy is close!");
            return target;
        }
        String scheme = target.getSchemeName();
        String host = target.getHostName();
        String targetDomain = scheme + "://" + host;
//        addExtraHeader(host, request);
        if (proxyProperties.getIgnoreDomains().contains(targetDomain)) {
            return target;
        }
        if (target.getPort() != -1) {
            targetDomain = targetDomain + ":" + target.getPort();
        }
        request.addHeader(PROXY_HEADER, targetDomain);
        request.addHeader(GW_FROM_APP, proxyProperties.getXesOrigin());
        return HttpHost.create(proxyProperties.getBaseURL());
    }

    private void rewriteToProxyIfNotIgnored(HttpRequestBase request) {
        if (!proxyProperties.getEnable()) {
            log.info("rewriteToProxyIfNotIgnored | proxy is close!");
            return;
        }
        URI targetURI = request.getURI();
        String host = targetURI.getHost();
        String scheme = targetURI.getScheme();
        String targetDomain = scheme + "://" + host;
//        addExtraHeader(host, request);

        if (proxyProperties.getIgnoreDomains().contains(targetDomain)) {
            return;
        }
        if (targetURI.getPort() != -1) {
            targetDomain = targetDomain + ":" + targetURI.getPort();
        }
        request.addHeader(PROXY_HEADER, targetDomain);
        request.addHeader(GW_FROM_APP, proxyProperties.getXesOrigin());
        String byProxyUrl = targetURI.toString().replace(targetDomain, proxyProperties.getBaseURL());
        request.setURI(URI.create(byProxyUrl));
    }



}
