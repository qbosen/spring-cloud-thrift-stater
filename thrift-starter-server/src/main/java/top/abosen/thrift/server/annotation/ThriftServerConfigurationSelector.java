package top.abosen.thrift.server.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */

@Slf4j
public class ThriftServerConfigurationSelector extends SpringFactoryImportSelector<EnableThriftServer> {
    @Override
    protected boolean isEnabled() {
        return true;
       /* Environment environment = getEnvironment();
        String serviceName = environment.getProperty(Constants.SERVER_SERVICE_NAME, String.class);
        ServiceMode serviceMode = environment.getProperty(Constants.SERVER_SERVICE_MODE, ServiceMode.class);
        Integer servicePort = getEnvironment().getProperty(Constants.SERVER_SERVICE_PORT, Integer.class);

        boolean enableAutoConfiguration = StringUtils.isNotBlank(serviceName) &&
                                          ArrayUtils.contains(ServiceMode.values(), serviceMode) &&
                                          Objects.nonNull(servicePort) &&
                                          servicePort > 0;
        if (enableAutoConfiguration) {
            log.info("Enable thrift server auto configuration, service name [{}], service model [{}], service port [{}]",
                    serviceName, serviceMode, servicePort);
        }
        return enableAutoConfiguration;*/
    }
}
