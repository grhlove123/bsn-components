package com.bsn.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * httpClient 相关配置文件
 * @author liuyong4 2019/4/3 17:42
 **/
@Data
@ConfigurationProperties(prefix = "httpclient")
public class HttpClientProperties {
    private static final boolean DEFAULT_DISABLE_SSL_VALIDATION = false;
    private static final int DEFAULT_MAX_CONNECTIONS = 200;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 50;
    private static final long DEFAULT_TIME_TO_LIVE = 900L;
    private static final TimeUnit DEFAULT_TIME_TO_LIVE_UNIT = TimeUnit.SECONDS;
    private static final boolean DEFAULT_FOLLOW_REDIRECTS = true;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 60000;
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 3000;
    private static final int DEFAULT_CONNECTION_TIMER_REPEAT = 30000;

    private boolean disableSslValidation = DEFAULT_DISABLE_SSL_VALIDATION;
    private boolean followRedirects = DEFAULT_FOLLOW_REDIRECTS;
    /**
     * 连接池最大连接数
     */
    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    /**
     * 每个路由的最大连接数
     */
    private int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
    /**
     * 空闲连接的空闲时间
     */
    private long timeToLive = DEFAULT_TIME_TO_LIVE;
    /**
     * 空闲连接的空闲时间单位
     */
    private TimeUnit timeToLiveUnit = DEFAULT_TIME_TO_LIVE_UNIT;
    /**
     * 建立连接的超时时间
     */
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    /**
     * 等待数据的超时时间
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    /**
     * 从连接池获取连接的超时时间
     */
    private int connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;
    /**
     * 定时清理空闲连接的间隔时间-时间单位毫秒
     */
    private int connectionTimerRepeat = DEFAULT_CONNECTION_TIMER_REPEAT;
}
