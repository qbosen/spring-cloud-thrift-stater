package top.abosen.thrift.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import top.abosen.thrift.client.pool.TransportKeyedObjectPool;
import top.abosen.thrift.client.properties.ThriftClientProperties;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 运行时获取
 * @author qiubaisen
 * @date 2021/6/26
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThriftClientContext {
    private static final Lock lock = new ReentrantLock();
    private static ThriftClientContext context;

    private ThriftClientProperties properties;
    private TransportKeyedObjectPool objectPool;
    private LoadBalancerClient loadBalancerClient;

    public static ThriftClientContext context(
            ThriftClientProperties properties,
            TransportKeyedObjectPool objectPool,
            LoadBalancerClient loadBalancerClient
    ) {
        context().properties = properties;
        context().objectPool = objectPool;
        context().loadBalancerClient = loadBalancerClient;
        return context;
    }

    public static ThriftClientContext context() {
        if (context == null) {
            try {
                lock.lock();
                if (context == null) {
                    context = new ThriftClientContext();
                }
            } finally {
                lock.unlock();
            }
        }
        return context;
    }

}
