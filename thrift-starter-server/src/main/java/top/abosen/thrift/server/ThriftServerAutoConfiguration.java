package top.abosen.thrift.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.server.annotation.ThriftService;
import top.abosen.thrift.server.exception.ThriftServerException;
import top.abosen.thrift.server.properties.DefaultThriftServerConfigure;
import top.abosen.thrift.server.properties.ThriftServerConfigure;
import top.abosen.thrift.server.properties.ThriftServerProperties;
import top.abosen.thrift.server.server.ThriftServer;
import top.abosen.thrift.server.server.ThriftServerConsulDiscoveryFactory;
import top.abosen.thrift.server.server.ThriftServerGroup;
import top.abosen.thrift.server.wrapper.ThriftServiceWrapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */

@Slf4j
@Configuration
@ConditionalOnProperty(value = Constants.SERVER_SERVICE_NAME, matchIfMissing = false)
@EnableConfigurationProperties(ThriftServerProperties.class)
@RequiredArgsConstructor
public class ThriftServerAutoConfiguration {

    public static final String DEFAULT_CONFIGURE = "defaultThriftServerConfigure";

    @Bean(name = DEFAULT_CONFIGURE)
    @ConditionalOnMissingBean
    public DefaultThriftServerConfigure defaultThriftServerConfigure() {
        return new DefaultThriftServerConfigure();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThriftServerGroup thriftServerGroup(
            ThriftServerProperties properties, ThriftServerConsulDiscoveryFactory discoveryFactory, ApplicationContext applicationContext) {
        if (CollectionUtils.isEmpty(properties.getServices())) {
            throw new ThriftServerException("没有相关服务的服务配置, 检查: spring.cloud.thrift.server.services");
        }
        // 检查配置类
        Map<String, ThriftServerConfigure> configureMap = new HashMap<>();
        for (ThriftServerProperties.Service service : properties.getServices()) {
            if (!applicationContext.containsBean(service.getConfigure())) {
                throw new ThriftServerException(String.format("服务 [%s] 指定的配置bean [%s] 不存在",
                        service.getServiceName(), service.getConfigure()));
            }
            Object configure = applicationContext.getBean(service.getConfigure());
            if (!(configure instanceof ThriftServerConfigure)) {
                throw new ThriftServerException(String.format("服务 [%s] 指定的配置bean [%s] 不是一个有效的 ThriftServerConfigure 类型",
                        service.getServiceName(), service.getConfigure()));
            }
            configureMap.put(service.getConfigure(), ((ThriftServerConfigure) configure));
        }

        // 获取所有服务业务
        List<ThriftServiceWrapper> serviceWrappers = Arrays.stream(applicationContext.getBeanNamesForAnnotation(ThriftService.class))
                .map(beanName -> {
                    Object bean = applicationContext.getBean(beanName);
                    Object target = bean;
                    if (AopUtils.isAopProxy(bean)) {
                        target = AopProxyUtils.getSingletonTarget(bean);
                    }
                    if (target == null) {
                        log.warn("Can't get proxy target: {}", bean.getClass());
                        target = bean;
                    }
                    ThriftService thriftService = target.getClass().getAnnotation(ThriftService.class);

                    return ThriftServiceWrapper.of(target, thriftService.version());
                }).collect(Collectors.toList());

        if (serviceWrappers.isEmpty()) {
            log.error("Can't search any thrift service annotated with @ThriftService");
            throw new ThriftServerException("Can not found any thrift service");
        }

        // 对每一个服务配置 都加载这些业务
        List<ThriftServer> thriftServers = properties.getServices().stream()
                .map(serviceProperties -> ThriftServer.createServer(serviceProperties,
                        configureMap.get(serviceProperties.getConfigure()),discoveryFactory, serviceWrappers))
                .collect(Collectors.toList());

        return new ThriftServerGroup(thriftServers);
    }

    @Bean
    @ConditionalOnConsulEnabled
    @ConditionalOnBean({ConsulServiceRegistry.class,
            AutoServiceRegistrationProperties.class,
            ConsulDiscoveryProperties.class,
            HeartbeatProperties.class})
    public ThriftServerConsulDiscoveryFactory thriftServerConsulDiscovery(
            ConsulServiceRegistry consulServiceRegistry,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            ConsulDiscoveryProperties discoveryProperties,
            ApplicationContext context,
            HeartbeatProperties heartbeatProperties
    ) {
        return new ThriftServerConsulDiscoveryFactory(
                consulServiceRegistry,
                autoServiceRegistrationProperties,
                discoveryProperties,
                context,
                heartbeatProperties);
    }
}
