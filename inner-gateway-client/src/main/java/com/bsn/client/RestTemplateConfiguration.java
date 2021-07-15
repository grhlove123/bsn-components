package com.bsn.client;

import com.bsn.client.httpClient.CustomHttpClient;
import com.bsn.client.properties.ProxyProperties;
import com.bsn.client.restTemplate.RestTemplateProxyInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

@Slf4j
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "proxy.enable")
@ConditionalOnClass({RestTemplate.class,HttpClient.class})
@EnableConfigurationProperties({ProxyProperties.class})
public class RestTemplateConfiguration implements ApplicationContextAware {

//    private final ScheduledExecutorService connectionManagerTimer =
//            Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("HttpConnectionManager-%d").setDaemon(true).build());

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        ProxyProperties proxyProperties = applicationContext.getBean(ProxyProperties.class);
        Objects.requireNonNull(proxyProperties,"proxy bean could not found!");
        // 获取RestTemplate实例列表，加入拦截器
        Map<String, RestTemplate> restTemplateMap = applicationContext.getBeansOfType(RestTemplate.class);
        if (!CollectionUtils.isEmpty(restTemplateMap)) {
            for (Map.Entry<String, RestTemplate> entry : restTemplateMap.entrySet()) {
                entry.getValue().getInterceptors().add(new RestTemplateProxyInterceptor(proxyProperties));
            }
        }
    }


    @Bean
    @ConditionalOnClass({HttpClient.class})
    public HttpClient httpClient(ProxyProperties proxyProperties) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(500);
        cm.setDefaultMaxPerRoute(500);
        builder.setConnectionManager(cm);
        HttpClient httpClient = builder.build();
        return new CustomHttpClient(httpClient, proxyProperties);
    }

}
