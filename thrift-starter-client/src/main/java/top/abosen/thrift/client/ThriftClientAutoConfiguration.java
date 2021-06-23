package top.abosen.thrift.client;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.abosen.thrift.common.signature.DefaultServiceSignatureGenerator;
import top.abosen.thrift.common.signature.ServiceSignatureGenerator;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Configuration
@ConditionalOnBean(DiscoveryClient.class)
@ConditionalOnDiscoveryEnabled
@AutoConfigureAfter(CommonsClientAutoConfiguration.class)
public class ThriftClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceSignatureGenerator signatureGenerator() {
        return new DefaultServiceSignatureGenerator();
    }
}
