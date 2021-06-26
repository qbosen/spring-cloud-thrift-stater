package top.abosen.thrift.client.pool;

import lombok.Value;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
@Value
public class ThriftServerNode {
    String host;
    int port;
    int timeout;
}
