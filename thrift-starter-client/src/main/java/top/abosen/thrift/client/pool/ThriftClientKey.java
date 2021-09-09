package top.abosen.thrift.client.pool;

import lombok.Value;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */

@Value
public class ThriftClientKey {
    String signature;
    String serviceName;

    String configure;

    ThriftServerNode node;

    public ThriftClientKey(String signature, String serviceName, String configure, String host, int port) {
        this.signature = signature;
        this.serviceName = serviceName;
        this.configure = configure;
        this.node = new ThriftServerNode(host, port);
    }
}
