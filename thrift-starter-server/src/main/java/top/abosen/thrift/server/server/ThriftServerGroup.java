package top.abosen.thrift.server.server;

import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * thrift 服务组
 *
 * @author qiubaisen
 * @date 2021/09/06
 */

@Value
public class ThriftServerGroup {
    Queue<ThriftServer> servers = new LinkedBlockingDeque<>();

    public ThriftServerGroup(ThriftServer... servers) {
        if (Objects.isNull(servers) || servers.length == 0) {
            return;
        }

        this.servers.clear();
        this.servers.addAll(Arrays.asList(servers));
    }

    public ThriftServerGroup(List<ThriftServer> servers) {
        if (CollectionUtils.isEmpty(servers)) {
            return;
        }

        this.servers.clear();
        this.servers.addAll(servers);
    }

    public Queue<ThriftServer> getServers() {
        return servers;
    }

}
