package top.abosen.thrift.client;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.pool.TransportKeyedObjectPool;
import top.abosen.thrift.client.pool.TransportKeyedPooledObjectFactory;
import top.abosen.thrift.client.properties.*;
import top.abosen.thrift.client.scanner.ThriftClientBeanScanProcessor;

import java.util.List;
import java.util.Objects;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Configuration
@ConditionalOnDiscoveryEnabled
@AutoConfigureAfter({CommonsClientAutoConfiguration.class, BlockingLoadBalancerClientAutoConfiguration.class})
@AutoConfigureOrder(Integer.MAX_VALUE)
@EnableConfigurationProperties(ThriftClientProperties.class)
public class ThriftClientAutoConfiguration {

    @Bean
    public ThriftClientConfigure defaultClientConfigure(LoadBalancerClient loadBalancerClient) {
        return new DefaultThriftClientConfigure(loadBalancerClient);
    }

    @Bean
    public ThriftClientConfigure compatibleClientConfigure(LoadBalancerClient loadBalancerClient) {
        return new CompatibleThriftClientConfigure(loadBalancerClient);
    }

    @Bean ThriftClientConfigureWrapper thriftClientConfigure(List<ThriftClientConfigure> configureList, ThriftClientProperties clientProperties) {
        if (CollectionUtils.isEmpty(configureList)) {
            throw new ThriftClientException("没有相关的 ThriftClientConfigure 配置");
        }
        clientProperties.getServices().stream()
                .filter(p -> configureList.stream().noneMatch(c -> Objects.equals(p.getConfigure(), c.configureName())))
                .findAny().ifPresent(prop -> {
                    throw new ThriftClientException(String.format("未找到服务[%s]相关的 ThriftClientConfigure 配置[%s]", prop.getServiceName(), prop.getConfigure()));
                });
        if (configureList.stream().map(ThriftClientConfigure::configureName).filter(StringUtils::isNoneBlank).distinct().count() != configureList.size()) {
            throw new ThriftClientException("存在无效的 ConfigureName");
        }
        return new ThriftClientConfigureWrapper(configureList);
    }


    @Bean
    @ConditionalOnMissingBean
    public static ThriftClientBeanScanProcessor thriftClientBeanScannerConfigurer() {
        // BeanFactoryPostProcessor objects must be instantiated very early in the container lifecycle
        return new ThriftClientBeanScanProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransportKeyedPooledObjectFactory transportKeyedPooledObjectFactory(
            ThriftClientConfigureWrapper clientConfigureWrapper, ThriftClientProperties clientProperties) {
        return new TransportKeyedPooledObjectFactory(clientConfigureWrapper, clientProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransportKeyedObjectPool transportKeyedObjectPool(
            GenericKeyedObjectPoolConfig<TTransport> config, TransportKeyedPooledObjectFactory poolFactory) {
        return new TransportKeyedObjectPool(poolFactory, config);
    }

    @Bean
    public GenericKeyedObjectPoolConfig<TTransport> keyedObjectPoolConfig(ThriftClientProperties properties) {
        ThriftClientProperties.Pool poolProperties = properties.getPool();
        GenericKeyedObjectPoolConfig<TTransport> config = new GenericKeyedObjectPoolConfig<>();
        config.setMinIdlePerKey(poolProperties.getPoolMinIdlePerKey());
        config.setMaxIdlePerKey(poolProperties.getPoolMaxIdlePerKey());
        config.setMaxWaitMillis(poolProperties.getPoolMaxWait());
        config.setMaxTotalPerKey(poolProperties.getPoolMaxTotalPerKey());
        config.setTestOnCreate(poolProperties.isTestOnCreate());
        config.setTestOnBorrow(poolProperties.isTestOnBorrow());
        config.setTestOnReturn(poolProperties.isTestOnReturn());
        config.setTestWhileIdle(poolProperties.isTestWhileIdle());
        config.setFairness(true);
        config.setJmxEnabled(false);
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public ThriftClientContext thriftClientContext(
            ThriftClientProperties properties,
            TransportKeyedObjectPool objectPool,
            ThriftClientConfigureWrapper clientConfigureWrapper
    ) {
        return ThriftClientContext.init(properties, objectPool, clientConfigureWrapper);
    }
}
