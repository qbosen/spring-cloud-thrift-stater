package top.abosen.thrift.server.server;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.server.TServer;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ThriftServer implements SmartLifecycle {
    TServer server;
    ThriftServerProperties properties;
    List<ThriftServiceWrapper> serviceWrappers;


    public static ThriftServer createServer(ThriftServerProperties properties, List<ThriftServiceWrapper> serviceWrappers) {
        TServer server = ThriftServerModeManager.createServerWithMode(properties, serviceWrappers);
        return new ThriftServer(server, properties, serviceWrappers);
    }

    @Override public boolean isAutoStartup() {
        return true;
    }

    @Override public void start() {
        log.debug("Starting thrift server [{}]", properties.getId());
        new Thread(server::serve, "thrift-server-thread").start();
        log.debug("Thrift server[{}] is started", properties.getId());

    }

    @Override public void stop() {
        log.debug("Stopping thrift server [{}]", properties.getId());
        server.stop();
        log.debug("Thrift server[{}] is stopped", properties.getId());
    }

    @Override public boolean isRunning() {
        return server.isServing();
    }
}
