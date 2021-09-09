package top.abosen.thrift.server;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.SmartLifecycle;
import top.abosen.thrift.server.server.ThriftServer;
import top.abosen.thrift.server.server.ThriftServerGroup;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qiubaisen
 * @date 2021/9/6
 */

@Slf4j
@Value
public class ThriftServerBootstrap implements SmartLifecycle {
    ThriftServerGroup thriftServerGroup;


    @Override public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        if (CollectionUtils.isEmpty(thriftServerGroup.getServers())) {
            return;
        }
        log.debug("Starting thrift servers");
        AtomicInteger serverIndex = new AtomicInteger(0);
        thriftServerGroup.getServers().forEach(server ->
                server.start("thrift-server-" + serverIndex.incrementAndGet())
        );
    }

    @Override public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override public void stop() {
        stop(() -> {
        });
    }

    @Override public void stop(Runnable callback) {
        if (isRunning()) {
            log.debug("Shutting down thrift servers");
            thriftServerGroup.getServers().forEach(ThriftServer::stop);
            callback.run();
        }
    }

    @Override public boolean isRunning() {
        return thriftServerGroup.getServers().stream().anyMatch(ThriftServer::isRunning);
    }

}
