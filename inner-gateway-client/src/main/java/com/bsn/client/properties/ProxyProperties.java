package com.bsn.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 内网关配置
 *
* @author guoronghua 2021/6/21 5:00 PM
*/
@Data
@ConfigurationProperties(prefix = "proxy")
public class ProxyProperties {

    private Boolean enable = false;
    /**
     * 访问proxy时携带request-id时需要增加当前系统特殊标识
     */
    private String tracePrefix;
    /**
     * 代理服务器的地址
     */
    private String baseURL;
    /**
     * 不转发的域名
     */
    private List<String> ignoreDomains = new ArrayList<>();

    private List<String> originUrls;

    private String xesOrigin;

    /**
     * 针对restTemplate方式，是否通过SDK注入拦截器，默认：true
     */
    private Boolean restTemplateInterceptor;

//    /**
//     * 针对restTemplate方式，是否通过SDK注入拦截器，默认：true
//     */
//    private Boolean httpWr;
    /**
     * 是否
     */
    private Boolean feignEnable = false;

}