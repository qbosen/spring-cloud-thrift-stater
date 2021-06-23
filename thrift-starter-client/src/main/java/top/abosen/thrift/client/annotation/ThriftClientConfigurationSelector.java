package top.abosen.thrift.client.annotation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.env.Environment;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.ServiceMode;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Slf4j
public class ThriftClientConfigurationSelector extends SpringFactoryImportSelector<EnableThriftClient> {
    @Override
    protected boolean isEnabled() {
        Environment environment = getEnvironment();
        String serviceName = environment.getProperty(Constants.CLIENT_SERVICE_NAME, String.class);
        ServiceMode serviceMode = environment.getProperty(Constants.CLIENT_SERVICE_MODE, ServiceMode.class);

        boolean enableAutoConfiguration = StringUtils.isNotBlank(serviceName) &&
                                          ArrayUtils.contains(ServiceMode.values(), serviceMode);
        if (enableAutoConfiguration) {
            log.info("Enable thrift client auto configuration, service name [{}], service model [{}]",
                    serviceName, serviceMode);
        }
        return enableAutoConfiguration;
    }
}
