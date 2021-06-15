package top.abosen.thrift.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.abosen.thrift.server.annotation.ThriftService;
import top.abosen.thrift.server.exception.ThriftServerException;
import top.abosen.thrift.server.properties.ThriftServerProperties;
import top.abosen.thrift.server.server.ThriftServer;
import top.abosen.thrift.server.wrapper.ThriftServiceWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */

@Slf4j
@Configuration
@ConditionalOnProperty(value = Constants.SERVER_ID, matchIfMissing = false)
@EnableConfigurationProperties(ThriftServerProperties.class)
@RequiredArgsConstructor
public class ThriftServerAutoConfiguration {
    final ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public ThriftServer thriftServerGroup(ThriftServerProperties properties) {

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
                    return ThriftServiceWrapper.of(properties.getId(), beanName, target, thriftService.version());
                }).collect(Collectors.toList());

        if (serviceWrappers.isEmpty()) {
            log.error("Can't search any thrift service annotated with @ThriftService");
            throw new ThriftServerException("Can not found any thrift service");
        }

        return ThriftServer.createServer(properties, serviceWrappers);
    }
}
