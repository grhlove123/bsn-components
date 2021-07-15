package com.bsn.client.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import java.io.IOException;

/**
 * loadBalance客户端增加标记能力
 */
public class MarkedLoadBalancerFeignClient extends LoadBalancerFeignClient {
    /**
     * 标记某个请求是否使用服务发现进行调用(loadBalanceClient)
     */
    static ThreadLocal<Boolean> LOAD_BALANCE_FLAG = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public MarkedLoadBalancerFeignClient(Client delegate,
                                         CachingSpringLoadBalancerFactory lbClientFactory,
                                         SpringClientFactory clientFactory) {
        super(delegate, lbClientFactory, clientFactory);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        LOAD_BALANCE_FLAG.set(true);
        return super.execute(request, options);
    }
}
