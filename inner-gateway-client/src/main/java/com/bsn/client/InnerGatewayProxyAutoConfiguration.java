package com.bsn.client;

import com.bsn.client.httpClient.CustomHttpClient;
import com.bsn.client.properties.ProxyProperties;
import com.bsn.client.restTemplate.RestTemplateProxyInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "proxy.enable")
@EnableConfigurationProperties({ProxyProperties.class})
//@ImportAutoConfiguration(value = {SentinelAdapterV1Configuration.class, SentinelAdapterV2ServletConfiguration.class, SentinelAdapterV2ReactConfiguration.class})
public class InnerGatewayProxyAutoConfiguration implements ApplicationContextAware {

//    private final ScheduledExecutorService connectionManagerTimer =
//            Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("HttpConnectionManager-%d").setDaemon(true).build());

    private CloseableHttpClient httpClient;

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

//    private SpringBeanUtil springBeanUtil;

//    @Autowired(required = false)
//    private FeignDecoderLogTemplate feignDecoderLogTemplate;
//
//    @Autowired
//    private ObjectFactory<HttpMessageConverters> messageConverters;

//    @Bean
//    @ConditionalOnClass({RestTemplate.class})
//    @ConditionalOnProperty(name = "proxy.restTemplateInterceptor",matchIfMissing = true)
//    public RestTemplate addProxyInterceptor(RestTemplate normalRestTemplate,ProxyProperties proxyProperties) {
//        if (Objects.isNull(normalRestTemplate)) {
//            log.info("restTemplate is null");
//            return null;
//        }
//        /**
//         * 增加拦截器
//         */
//        normalRestTemplate.getInterceptors().add(new RestTemplateProxyInterceptor(proxyProperties));
//        return normalRestTemplate;
//    }

//    @Bean
//    public SpringBeanUtil springBeanUtil() {
//        springBeanUtil = new SpringBeanUtil();
//        return springBeanUtil;
//    }

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


//
//    /**
//     * feign 日志相关插件
//     */
//    @Bean
//    @ConditionalOnClass({Decoder.class})
////    @ConditionalOnMissingBean
////    @ConditionalOnProperty(name = "feign.custom.decoder.enabled", matchIfMissing = false)
//    public Decoder feignDecoder() {
//        log.info("load custom log feign decoder");
//        return new LogSpringDecoder(new SpringDecoder(this.messageConverters), feignDecoderLogTemplate);
//    }
//
//    @Bean
//    @ConditionalOnClass({ErrorDecoder.class})
////    @ConditionalOnMissingBean
////    @ConditionalOnProperty(name = "feign.custom.errorDecoder.enabled", matchIfMissing = false)
//    public ErrorDecoder feignErrorDecoder() {
//        log.info("load custom log feign error decoder");
//        return new LogErrorDecoder();
//    }
//
//    @Bean
//    @ConditionalOnClass({Request.Options.class})
////    @ConditionalOnMissingBean
////    @ConditionalOnProperty(name = "feign.custom.options.enabled", matchIfMissing = false)
//    Request.Options options(HttpClientProperties httpClientProperties) {
//        return new Request.Options(httpClientProperties.getConnectionTimeout(), httpClientProperties.getSocketTimeout());
//    }
//
//    @Bean
//    @ConditionalOnClass({Logger.Level.class})
//    @ConditionalOnMissingBean
//    Logger.Level feignLoggerLevel() {
//        return Logger.Level.FULL;
//    }

//    /**
//     * httpClient 相关配置
//     */
//    @PreDestroy
//    public void destroy() throws Exception {
//        connectionManagerTimer.shutdown();
//        if(httpClient != null) {
//            httpClient.close();
//        }
//    }

//    @Bean
//    public HttpClientProperties httpClientProperties() {
//        return new HttpClientProperties();
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public HttpClientConnectionManager connectionManager(
//            ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
//            HttpClientProperties httpClientProperties) {
//
//        final HttpClientConnectionManager connectionManager = connectionManagerFactory
//                .newConnectionManager(httpClientProperties.isDisableSslValidation(), httpClientProperties.getMaxConnections(),
//                        httpClientProperties.getMaxConnectionsPerRoute(),
//                        httpClientProperties.getTimeToLive(),
//                        httpClientProperties.getTimeToLiveUnit(), null);
//
//        // 定时清理空闲超时的连接
//        this.connectionManagerTimer.scheduleWithFixedDelay(new TimerTask() {
//            @Override
//            public void run() {
//                connectionManager.closeExpiredConnections();
//            }
//        }, 30000, httpClientProperties.getConnectionTimerRepeat(), TimeUnit.MILLISECONDS);
//        return connectionManager;
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public CloseableHttpClient customHttpClient(HttpClientConnectionManager httpClientConnectionManager,
//                                                HttpClientProperties httpClientProperties) {
//        log.info("load xes custom http client");
//
//        HttpClientBuilder builder = HttpClientBuilder.create().disableCookieManagement().useSystemProperties();
//        this.httpClient = createClient(builder, httpClientConnectionManager, httpClientProperties);
//        return this.httpClient;
//    }

//    /**
//     * 覆盖ApacheHttpClient,因为有需要处理proxy
//     * @param httpClient CloseableHttpClient
//     * @return 自定义的httpClient
//     * 集成loadBalance能力,参考 HttpClientFeignLoadBalancedConfiguration
//     *
//     * 服务发现调用先会使用MarkedLoadBalancerFeignClient，直接url访问会直接使用ProxyHttpClient
//     */
//    @Bean
//    @ConditionalOnMissingBean
//    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory, SpringClientFactory clientFactory,
//                              HttpClient httpClient, ProxyProperties proxyProperties) {
//        return new MarkedLoadBalancerFeignClient(new ProxyHttpClient(httpClient, proxyProperties), cachingFactory, clientFactory);
//    }
//
//    private CloseableHttpClient createClient(HttpClientBuilder builder, HttpClientConnectionManager httpClientConnectionManager,
//                                             HttpClientProperties httpClientProperties) {
//        RequestConfig defaultRequestConfig = RequestConfig.custom()
//                .setConnectTimeout(httpClientProperties.getConnectionTimeout())
//                .setSocketTimeout(httpClientProperties.getSocketTimeout())
//                .setConnectionRequestTimeout(httpClientProperties.getConnectionRequestTimeout())
//                .setRedirectsEnabled(httpClientProperties.isFollowRedirects())
//                .build();
//
//        return builder.setDefaultRequestConfig(defaultRequestConfig).
//                setConnectionManager(httpClientConnectionManager)
//                .build();
//    }


}
