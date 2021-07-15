package com.bsn.client;

import com.bsn.client.properties.ProxyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "proxy.enable")
@EnableConfigurationProperties({ProxyProperties.class})
@ImportAutoConfiguration(value = {RestTemplateConfiguration.class,CustomFeignConfiguration.class})
public class InnerGatewayProxyAutoConfiguration {

//    private final ScheduledExecutorService connectionManagerTimer =
//            Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("HttpConnectionManager-%d").setDaemon(true).build());




}
