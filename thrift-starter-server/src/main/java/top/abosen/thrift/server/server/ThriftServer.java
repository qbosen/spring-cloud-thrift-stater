package top.abosen.thrift.server.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.server.TServer;
import top.abosen.thrift.server.exception.ThriftServerExceptionConverter;
import top.abosen.thrift.server.properties.ThriftServerConfigure;
import top.abosen.thrift.server.properties.ThriftServerProperties;
import top.abosen.thrift.server.wrapper.ThriftServiceWrapper;

import java.util.List;

/**
 * thrift 服务容器, 带生命周期控制
 *
 * @author qiubaisen
 * @date 2021/6/16
 */

@Getter
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ThriftServer {
    final TServer server;
    final ThriftServerProperties.Service properties;
    final List<ThriftServiceWrapper> serviceWrappers;
    final ThriftServerConsulDiscovery consulDiscovery;


    public static ThriftServer createServer(
            ThriftServerProperties.Service properties,
            ThriftServerConfigure serverConfigure,
            ThriftServerConsulDiscoveryFactory discoveryFactory,
            List<ThriftServiceWrapper> serviceWrappers,
            ThriftServerExceptionConverter exceptionConverter
            ) {
        TServer server = ThriftServerModeManager.createServerWithMode(properties, serverConfigure, serviceWrappers,exceptionConverter);
        return new ThriftServer(server, properties, serviceWrappers, discoveryFactory.createConsulDiscovery(properties));
    }

    public void start(String threadName) {
        log.debug("Starting thrift server [{}]", properties.getServiceName());
        new Thread(server::serve, StringUtils.isNotEmpty(threadName) ? threadName : "thrift-server-thread").start();
        consulDiscovery.registerService();
        log.debug("Thrift server[{}] is started", properties.getServiceName());
    }

    public void stop() {
        log.debug("Stopping thrift server [{}]", properties.getServiceName());
        server.stop();
        consulDiscovery.unregisterService();
        log.debug("Thrift server[{}] is stopped", properties.getServiceName());
    }

    public boolean isRunning() {
        return server.isServing();
    }
}
