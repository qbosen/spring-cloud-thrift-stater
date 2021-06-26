package top.abosen.thrift.server.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.server.TServer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
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
public class ThriftServer implements SmartLifecycle, ApplicationContextAware {
    final TServer server;
    final ThriftServerProperties properties;
    final List<ThriftServiceWrapper> serviceWrappers;

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static ThriftServer createServer(ThriftServerProperties properties, List<ThriftServiceWrapper> serviceWrappers) {
        TServer server = ThriftServerModeManager.createServerWithMode(properties, serviceWrappers);
        return new ThriftServer(server, properties, serviceWrappers);
    }

    @Override public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        log.debug("Starting thrift server [{}]", properties.getServiceName());
        new Thread(server::serve, "thrift-server-thread").start();
        log.debug("Thrift server[{}] is started", properties.getServiceName());
        applicationContext.publishEvent(new ThriftServerStartEvent(applicationContext));
    }

    @Override public void stop() {
        log.debug("Stopping thrift server [{}]", properties.getServiceName());
        server.stop();
        log.debug("Thrift server[{}] is stopped", properties.getServiceName());
        applicationContext.publishEvent(new ThriftServerStopEvent(applicationContext));
    }

    @Override public boolean isRunning() {
        return server.isServing();
    }
}
