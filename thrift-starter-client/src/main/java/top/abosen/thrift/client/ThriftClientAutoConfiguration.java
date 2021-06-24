package top.abosen.thrift.client;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public ThriftClientBeanScanProcessor thriftClientBeanScannerConfigurer() {
        return new ThriftClientBeanScanProcessor();
    }
}
