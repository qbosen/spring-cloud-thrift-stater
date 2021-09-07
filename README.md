# Spring cloud thrift starter

## 简介

`spring-cloud-thrift-starter`提供`Spring Cloud`对可伸缩的跨语言服务调用框架`Apache Thrift`的封装和集成。

`spring-cloud-thrift-starter`包括客户端`thrift-starter-client`和服务端`thrift-starter-server`两个模块。

**服务端：**

1. 支持`Apache Thrift`的各种原生服务线程模型，包括单线程阻塞模型(`simple`)、单线程非阻塞模型(`nonBlocking`)、线程池阻塞模型(`threadPool`)、半同步半异步模型(`hsHa`)和半同步半异步线程选择器模型(`threadedSelector`)。
2. 支持由配置服务签名，默认(服务`ID` + 客户端`Stub`接口名称 + 服务版本号)唯一标识服务`Stub`的具体实现类。
3. 支持与`Spring Cloud Consul`集成，支持服务注册，健康检查
4. 支持自定义 `ThriftServerConfigure`，自定义服务签名、深度配置底层协议
5. 支持对相同业务启动多个 `thrift` 服务，每个服务支持自定义 `ThriftServerConfigure`，一个业务服务多个不同版本的客户端。

**客户端：**

1. 支持自定义 `ThriftClientConfigure`, 配置服务签名、深度配置底层协议，与不同协议的旧服务端兼容。
2. 支持`Apache Thrift`的`Transport`层的连接池管理，减少了客户端与服务端之间连接的频繁创建和销毁。
3. 支持与`Spring Cloud Consul`集成，客户端通过心跳检测与服务注册中心`Consul`保持连接，动态定时的刷新服务列表、监测服务的启用、关闭和健康状态。
4. 支持与`Spring Cloud LoadBalancer`集成，支持客户端负载均衡，使用轮询的负载均衡策略，客户端的`Thrift`程序通过本地的服务缓存列表实现调用的动态转发。

> 兼容模式
>
> 提供了默认的 configure = compatible, 支持老服务的兼容模式
>
> 1. 签名方式使用 `serviceClass.getSimpleName()` 存在重复的风险
> 2. 传输协议使用 `TBinaryProtocal` 性能更低
> 3. 服务是如果以 `api` 结尾的服务，则端口`+1`
> 4. 服务端模式需要配置为 `thread_pool`

## 使用

可以参考完整的 [calculator demo](thrift-starter-demo/calculator)

### 服务端

#### 1. 依赖

```
implementation("top.abosen:thrift-starter-server:1.0.1")
implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
```

#### 2. 配置

参考 `top.abosen.thrift.server.properties.ThriftServerProperties`

```yaml
spring:
  application:
    name: calculator-thrift-pure-server
  cloud:
    consul:
      host: "${CONSUL_HOST:localhost}"
      port: "${CONSUL_PORT:8500}"
      discovery:
        enabled: true
        register: false
        register-health-check: false
        query-passing: true
    thrift:
      server:
        services:
          - service-name: calculator-thrift-server 	# 服务注册名称，必填
            service-mode: threaded_selector				 	# 服务端模式，可选，默认 threaded_selector
            service-port: 8081											# 服务端端口，必填
            queue-size: 1000												# 任务队列大小，可选，默认1000
						# 服务配置，可选，默认default。 通过实现 `ThriftServerConfigure`接口进行自定义配置，内置default/compatible
						configure: default											
            discovery:
              register: true												# 是否进行服务注册，可选，默认false
              health-check: true										# 是否进行健康检查，可选，默认true
              prefer-ip-address: true								# 注册时候使用的地址是ip/host，可选，默认true
              # 注册使用的实例id，必填
              instance-id: ${spring.cloud.thrift.server.services[0].service-name}:${spring.cloud.client.ip-address}:${spring.cloud.thrift.server.services[0].service-port}
              # 注册的tag，可选
              tags:
                - calculator
                - thrift
            # 服务模式相关配置，可选[simple,non-blocking,thread-pool,hs-ha,threaded-selector]，与service-mode对应
            threaded-selector:
              min-worker-threads: 5
              max-worker-threads: 20
              keep-alive-time: 200
          - service-name: calculator-thrift-server-compatible			# 对相同业务启动多个thrift服务
            service-mode: thread_pool
            service-port: 8082																		# 不同的服务端口
            queue-size: 1000
            # 服务配置为内置的兼容模式，参考 `CompatibleThriftServerConfigure` 
            configure: compatible
            discovery:
              register: true
              health-check: true
              prefer-ip-address: true
              instance-id: ${spring.cloud.thrift.server.services[1].service-name}:${spring.cloud.client.ip-address}:${spring.cloud.thrift.server.services[1].service-port}
              tags:
                - calculator
                - thrift
            thread-pool:
              min-worker-threads: 5
              max-worker-threads: 20
              keep-alive-time: 200
```

#### 3. 代码

`@ThriftService(version=?)` 暂不支持版本设置，保持默认即可

```java
@ThriftService
@RequiredArgsConstructor
public class CalculatorThriftController implements CalculatorService.Iface {
    final CalculatorAppService calculatorAppService;

    @Override
    public int add(int arg1, int arg2) {
        return calculatorAppService.add(arg1, arg2);
    }

    @Override
    public int subtract(int arg1, int arg2) {
        return calculatorAppService.subtract(arg1, arg2);
    }

    @Override
    public int multiply(int arg1, int arg2) {
        return calculatorAppService.multiply(arg1, arg2);
    }

    @Override
    public int division(int arg1, int arg2) {
        return calculatorAppService.division(arg1, arg2);
    }
}

@SpringBootApplication
@EnableDiscoveryClient
@EnableThriftServer
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 客户端

#### 1. 依赖

```
implementation("top.abosen:thrift-starter-client:1.0.1")
implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
```

#### 2. 配置

参考 `top.abosen.thrift.client.properties.ThriftClientProperties`

参考配置:

```yaml
spring:
  application:
    name: calculator-rest-client
  cloud:
    consul:
      host: "${CONSUL_HOST:localhost}"
      port: "${CONSUL_PORT:8500}"
      discovery:
        enabled: true
        register: true
        prefer-ip-address: true
        instance-id: ${spring.application.name}:${spring.cloud.client.ip-address}:${server.port}
        tags:
          - calculator
          - http
        health-check-path: /actuator/health
        register-health-check: true
        health-check-interval: 10s
        query-passing: true
    thrift:
      client:
        pool:                           # 客户端调用池配置，可选
          retry-times: 3                # 负载调用重试此时
          connect-timeout: 10000        # socket 超时配置
          pool-max-total-per-key: 60    # 池化配置
          pool-max-idle-per-key: 40
          pool-min-idle-per-key: 3
          pool-max-wait: 180000
          test-on-create: true
          test-on-borrow: true
          test-on-return: true
          is-test-while-idle: true
        services:
          - service-name: calculator-thrift-server            # 服务名称 
          	# 客户端配置，可选，默认default。 通过实现 `ThriftClientConfigure`接口进行自定义配置，内置default/compatible
            configure: default																
            service-mode: threaded_selector                   # 服务端模式，默认 threaded_selector
            package-to-scan: top.abosen.thrift.demo.calculator  # 扫描的iface包路径，多个使用,分割
          - service-name: other-server                        # 多个服务配置
            package-to-scan: other.server
            configure: compatible															# 访问兼容模式的服务端
```

#### 3. 代码

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableThriftClient
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

