package com.bsn.client;

import com.bsn.client.feign.LogErrorDecoder;
import com.bsn.client.feign.LogSpringDecoder;
import com.bsn.client.feign.ProxyHttpClient;
import com.bsn.client.properties.HttpClientProperties;
import com.bsn.client.properties.ProxyProperties;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import feign.Client;
import feign.Logger;
import feign.Request;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 集成所有相关配置项
 * @author liuyong4 2019/4/3 17:41
 **/
@Slf4j
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "proxy.enable")
@ConditionalOnClass({Decoder.class,ErrorDecoder.class,Request.Options.class})
public class CustomFeignConfiguration {
    private final ScheduledExecutorService connectionManagerTimer = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("HttpConnectionManager-%d").setDaemon(true).build());

    private CloseableHttpClient httpClient;

    private final ObjectFactory<HttpMessageConverters> messageConverters;

    @Autowired
    public CustomFeignConfiguration(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    /**
     * feign 日志相关插件
     */
    @Bean
    @ConditionalOnMissingBean
    public Decoder feignDecoder() {
        log.info("load custom log feign decoder");
        return new LogSpringDecoder(new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)));
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder feignErrorDecoder() {
        log.info("load custom log feign error decoder");
        return new LogErrorDecoder();
    }

    @Bean
    @ConditionalOnMissingBean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @ConditionalOnMissingBean
    Request.Options options(HttpClientProperties customHttpClientProperties) {
        return new Request.Options(customHttpClientProperties.getConnectionTimeout(), customHttpClientProperties.getSocketTimeout());
    }

    /**
     * httpClient 相关配置
     */
    @PreDestroy
    public void destroy() throws Exception {
        connectionManagerTimer.shutdown();
        if(httpClient != null) {
            httpClient.close();
        }
    }

    @Bean
    public HttpClientProperties customHttpClientProperties() {
        return new HttpClientProperties();
    }


    @Bean
    @ConditionalOnMissingBean
    public HttpClientConnectionManager connectionManager(
            ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
            @Autowired HttpClientProperties customHttpClientProperties) {

        final HttpClientConnectionManager connectionManager = connectionManagerFactory
                .newConnectionManager(customHttpClientProperties.isDisableSslValidation(), customHttpClientProperties.getMaxConnections(),
                        customHttpClientProperties.getMaxConnectionsPerRoute(),
                        customHttpClientProperties.getTimeToLive(),
                        customHttpClientProperties.getTimeToLiveUnit(), null);

        // 定时清理空闲超时的连接
        this.connectionManagerTimer.scheduleWithFixedDelay(new TimerTask() {
            @Override
            public void run() {
                connectionManager.closeExpiredConnections();
            }
        }, 30000, customHttpClientProperties.getConnectionTimerRepeat(), TimeUnit.MILLISECONDS);
        return connectionManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public CloseableHttpClient customHttpClient(HttpClientConnectionManager httpClientConnectionManager,
                                                @Autowired HttpClientProperties customHttpClientProperties) {
        log.info("load xes custom http client");

        HttpClientBuilder builder = HttpClientBuilder.create().disableCookieManagement().useSystemProperties();
        this.httpClient = createClient(builder, httpClientConnectionManager, customHttpClientProperties);
        return this.httpClient;
    }

    /**
     * 覆盖ApacheHttpClient,因为有需要处理proxy
     * @param customHttpClient CloseableHttpClient
     * @return 自定义的httpClient
     */
    @Bean
    @ConditionalOnMissingBean
    public Client feignClient(HttpClient customHttpClient, ProxyProperties customProxyProperties) {
        return new ProxyHttpClient(new ApacheHttpClient(customHttpClient), customProxyProperties);
    }

    private CloseableHttpClient createClient(HttpClientBuilder builder, HttpClientConnectionManager httpClientConnectionManager,
                                             HttpClientProperties customHttpClientProperties) {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(customHttpClientProperties.getConnectionTimeout())
                .setSocketTimeout(customHttpClientProperties.getSocketTimeout())
                .setConnectionRequestTimeout(customHttpClientProperties.getConnectionRequestTimeout())
                .setRedirectsEnabled(customHttpClientProperties.isFollowRedirects())
                .build();

        return builder.setDefaultRequestConfig(defaultRequestConfig).
                setConnectionManager(httpClientConnectionManager)
                .build();
    }


}
