package top.abosen.thrift.client;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.abosen.thrift.client.pool.TransportKeyedObjectPool;
import top.abosen.thrift.client.pool.TransportKeyedPooledObjectFactory;
import top.abosen.thrift.client.properties.CompatibleThriftClientConfigure;
import top.abosen.thrift.client.properties.DefaultThriftClientConfigure;
import top.abosen.thrift.client.properties.ThriftClientConfigure;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.client.scanner.ThriftClientBeanScanProcessor;
import top.abosen.thrift.common.Constants;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Configuration
@ConditionalOnDiscoveryEnabled
@AutoConfigureAfter(CommonsClientAutoConfiguration.class)
@AutoConfigureOrder(Integer.MAX_VALUE)
@EnableConfigurationProperties(ThriftClientProperties.class)
public class ThriftClientAutoConfiguration {

    @Bean(Constants.DEFAULT_CONFIGURE + Constants.CONFIGURE_BEAN_SUFFIX)
    public ThriftClientConfigure defaultClientConfigure(LoadBalancerClient loadBalancerClient) {
        return new DefaultThriftClientConfigure(loadBalancerClient);
    }

    @Bean(Constants.COMPATIBLE_CONFIGURE + Constants.CONFIGURE_BEAN_SUFFIX)
    public ThriftClientConfigure compatibleClientConfigure(LoadBalancerClient loadBalancerClient) {
        return new CompatibleThriftClientConfigure(loadBalancerClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public static ThriftClientBeanScanProcessor thriftClientBeanScannerConfigurer() {
        // BeanFactoryPostProcessor objects must be instantiated very early in the container lifecycle
        return new ThriftClientBeanScanProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransportKeyedPooledObjectFactory transportKeyedPooledObjectFactory(ThriftClientConfigure thriftClientConfigure, ThriftClientProperties clientProperties) {
        return new TransportKeyedPooledObjectFactory(thriftClientConfigure, clientProperties);
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
            ThriftClientConfigure thriftClientConfigure
    ) {
        return ThriftClientContext.init(properties, objectPool, thriftClientConfigure);
    }
}
