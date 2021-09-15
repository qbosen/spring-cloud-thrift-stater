package top.abosen.thrift.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.abosen.thrift.server.annotation.ThriftService;
import top.abosen.thrift.server.exception.ThriftServerException;
import top.abosen.thrift.server.properties.*;
import top.abosen.thrift.server.server.ThriftServer;
import top.abosen.thrift.server.server.ThriftServerConsulDiscoveryFactory;
import top.abosen.thrift.server.server.ThriftServerGroup;
import top.abosen.thrift.server.wrapper.ThriftServiceWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */

@Slf4j
@Configuration
@EnableConfigurationProperties(ThriftServerProperties.class)
@RequiredArgsConstructor
public class ThriftServerAutoConfiguration {

    @Bean
    public ThriftServerConfigure defaultThriftServerConfigure() {
        return new DefaultThriftServerConfigure();
    }

    @Bean
    public ThriftServerConfigure compatibleThriftServerConfigure() {
        return new CompatibleThriftServerConfigure();
    }


    @Bean ThriftServerConfigureWrapper thriftServerConfigure(List<ThriftServerConfigure> configureList, ThriftServerProperties serverProperties) {
        if (CollectionUtils.isEmpty(configureList)) {
            throw new ThriftServerException("没有相关的 ThriftServerConfigure 配置");
        }
        if (CollectionUtils.isEmpty(serverProperties.getServices())) {
            throw new ThriftServerException("没有相关服务的服务配置, 检查: spring.cloud.thrift.server.services");
        }
        serverProperties.getServices().stream()
                .filter(p -> configureList.stream().noneMatch(c -> Objects.equals(p.getConfigure(), c.configureName())))
                .findAny().ifPresent(prop -> {
                    throw new ThriftServerException(String.format("未找到服务[%s]相关的 ThriftServerConfigure 配置[%s]", prop.getServiceName(),
                            prop.getConfigure()));
                });
        if (configureList.stream().map(ThriftServerConfigure::configureName).filter(StringUtils::isNoneBlank).distinct().count() != configureList.size()) {
            throw new ThriftServerException("存在无效的 ConfigureName");
        }
        return new ThriftServerConfigureWrapper(configureList);
    }


    @Bean
    @ConditionalOnMissingBean
    public ThriftServerGroup thriftServerGroup(
            ThriftServerProperties properties,
            ThriftServerConsulDiscoveryFactory discoveryFactory,
            ThriftServerConfigureWrapper serverConfigureWrapper,
            ApplicationContext applicationContext) {
        if (CollectionUtils.isEmpty(properties.getServices())) {
            throw new ThriftServerException("没有相关服务的服务配置, 检查: spring.cloud.thrift.server.services");
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

                    return ThriftServiceWrapper.of(target,bean, thriftService.version());
                }).collect(Collectors.toList());

        if (serviceWrappers.isEmpty()) {
            log.error("Can't search any thrift service annotated with @ThriftService");
            throw new ThriftServerException("Can not found any thrift service");
        }

        // 对每一个服务配置 都加载这些业务
        List<ThriftServer> thriftServers = properties.getServices().stream()
                .map(serviceProperties -> ThriftServer.createServer(serviceProperties,
                        serverConfigureWrapper.getConfigure(serviceProperties.getConfigure()),
                        discoveryFactory,
                        serviceWrappers))
                .collect(Collectors.toList());

        return new ThriftServerGroup(thriftServers);
    }

    @Bean
    @ConditionalOnMissingBean
    public ThriftServerBootstrap thriftServerBootstrap(ThriftServerGroup thriftServerGroup) {
        return new ThriftServerBootstrap(thriftServerGroup);
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
