package com.bsn.client.restTemplate;

import com.bsn.client.properties.ProxyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;

/**
 * rest 拦截器，请求转发到 proxy
 *
* @author guoronghua 2021/6/21 7:13 PM
*/
@Slf4j
public class RestTemplateProxyInterceptor implements ClientHttpRequestInterceptor {


	private static final String PROXY_HEADER = "target-domain";

	private ProxyProperties proxyProperties;

	public RestTemplateProxyInterceptor(ProxyProperties proxyProperties) {
		this.proxyProperties = proxyProperties;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		/**
		 * 首先查询proxy开关是否开启
		 */
		if (!proxyProperties.getEnable()) {
			return execution.execute(request, body);
		}

		/**
		 * 获取原有hots以及相关配置
		 */
		URI originUri = request.getURI();
		String methodValue = request.getMethodValue();
		HttpHeaders headers = request.getHeaders();

		String originHost = originUri.getHost();
		/**
		 * 查看是否对此domain进行忽略处理
		 */
		if (proxyProperties.getIgnoreDomains().contains(originHost)){
			log.info("RestTemplateProxyInterceptor domain {} ignore.", originHost);
			return execution.execute(request, body);
		}

		String scheme = originUri.getScheme();
		String targetDomain = scheme + "://" + originHost;
		headers.add(PROXY_HEADER, targetDomain);

		String proxyUriStr = proxyProperties.getBaseURL() + originUri.getPath();
		if (!StringUtils.isEmpty(originUri.getQuery())) {
			proxyUriStr = proxyUriStr + "?" + originUri.getQuery();
		}
		URI proxyUri = URI.create(proxyUriStr);
		return execution.execute(requestBuilder(proxyUri, methodValue, headers), body);
	}

	HttpRequest requestBuilder(URI originUrl, String methodValue, HttpHeaders headers){
		return new HttpRequest() {
			@Override
			public String getMethodValue() {
				return methodValue;
			}

			@Override
			public URI getURI() {
				return originUrl;
			}

			@Override
			public HttpHeaders getHeaders() {
				return headers;
			}
		};
	}
}