#### 需在pom中配置maven私库
```xml
   <repositories>
        <repository>
            <id>xesapp-nexus</id>
            <name>maven-central</name>
            <url>http://39.96.133.237:8081/repository/maven-public</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
            <releases>
                <enabled>true</enabled>
            </releases>
        </repository>
    </repositories>
```

<br>
xes-feign作用: 将feign 公共的配置文件集中
xes-feign spring boot 2.x代码分支: feature/feign2

1.1 如果启动使用SpringApplication类:
```java
SpringApplication.run(new Object[]{CustomFeignConfiguration.class}, args);
```

1.2.如果启动使用SpringApplicationBuilder类
```java
new SpringApplicationBuilder().sources(CustomFeignConfiguration.class).run(args);
```
2.在启动类上添加 @EnableFeignClients

<br>
扩展-(请求发送之前操作header之类操作)
```java
//添加拦截器的方式--针对单个feign客户端
@FeignClient(name = "localFeign", url = "http://localhost:10010", configuration = LocalFeign.LocalFeignConfig.class)
public interface LocalFeign {

    @GetMapping(value = "/a.json")
    String getV();

    class LocalFeignConfig {

        @Bean
        RequestInterceptor externalInterceptor() {
            return new RequestInterceptor() {
                @Override
                public void apply(RequestTemplate template) {
                    template.header("external", "haha");
                }
            };
        }
    }
}
```