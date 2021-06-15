package top.abosen.thrift.server.annotation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.env.Environment;
import top.abosen.thrift.server.Constants;
import top.abosen.thrift.server.properties.ServerMode;

import java.util.Objects;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */

@Slf4j
public class ThriftServerConfigurationSelector extends SpringFactoryImportSelector<EnableThriftServer> {
    @Override
    protected boolean isEnabled() {
        Environment environment = getEnvironment();
        String serverId = environment.getProperty(Constants.SERVER_ID, String.class);
        String serverMode = environment.getProperty(Constants.SERVER_MODE, String.class);
        Integer serverPort = getEnvironment().getProperty(Constants.SERVER_PORT, Integer.class);

        boolean enableAutoConfiguration = StringUtils.isNotBlank(serverId) &&
                                          ArrayUtils.contains(ServerMode.ALL_MODE, serverMode) &&
                                          Objects.nonNull(serverPort) &&
                                          serverPort > 0;
        if (enableAutoConfiguration) {
            log.info("Enable thrift server auto configuration, server id {}, server model {}, serverPort {}",
                    serverId, serverMode, serverPort);
        }
        return enableAutoConfiguration;
    }
}
