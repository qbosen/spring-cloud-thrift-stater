package top.abosen.thrift.client;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.abosen.thrift.client.pool.TransportKeyedObjectPool;
import top.abosen.thrift.client.pool.TransportKeyedPooledObjectFactory;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.client.scanner.ThriftClientBeanScanProcessor;
import top.abosen.thrift.common.signature.DefaultServiceSignatureGenerator;
import top.abosen.thrift.common.signature.ServiceSignatureGenerator;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Configuration
// todo 恢复
//@ConditionalOnBean(DiscoveryClient.class)
//@ConditionalOnDiscoveryEnabled
//@AutoConfigureAfter(CommonsClientAutoConfiguration.class)
@AutoConfigureOrder(Integer.MAX_VALUE)
@EnableConfigurationProperties(ThriftClientProperties.class)
public class ThriftClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceSignatureGenerator signatureGenerator() {
        return new DefaultServiceSignatureGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public static ThriftClientBeanScanProcessor thriftClientBeanScannerConfigurer() {
        // BeanFactoryPostProcessor objects must be instantiated very early in the container lifecycle
        return new ThriftClientBeanScanProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransportKeyedPooledObjectFactory transportKeyedPooledObjectFactory(ThriftClientProperties properties) {
        return new TransportKeyedPooledObjectFactory(properties);
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
    @ConditionalOnBean(LoadBalancerClient.class)
    public ThriftClientContext thriftClientContext(
            ThriftClientProperties properties,
            TransportKeyedObjectPool objectPool,
            LoadBalancerClient loadBalancerClient,
            ServiceSignatureGenerator signatureGenerator
    ) {
        return ThriftClientContext.init(properties, objectPool, loadBalancerClient, signatureGenerator);
    }
}
