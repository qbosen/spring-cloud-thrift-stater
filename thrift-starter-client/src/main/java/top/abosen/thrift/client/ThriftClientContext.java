package top.abosen.thrift.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.pool.TransportKeyedObjectPool;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.common.signature.ServiceSignatureGenerator;

/**
 * 运行时获取
 *
 * @author qiubaisen
 * @date 2021/6/26
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThriftClientContext {
    static final ThriftClientContext CONTEXT = new ThriftClientContext();
    volatile boolean init;

    ThriftClientProperties properties;
    TransportKeyedObjectPool objectPool;
    LoadBalancerClient loadBalancerClient;
    ServiceSignatureGenerator signatureGenerator;

    public static synchronized ThriftClientContext init(
            ThriftClientProperties properties,
            TransportKeyedObjectPool objectPool,
            LoadBalancerClient loadBalancerClient,
            ServiceSignatureGenerator signatureGenerator) {
        if (CONTEXT.init) {
            throw new ThriftClientException("不可重复初始化");
        }
        CONTEXT.properties = properties;
        CONTEXT.objectPool = objectPool;
        CONTEXT.loadBalancerClient = loadBalancerClient;
        CONTEXT.signatureGenerator = signatureGenerator;

        CONTEXT.init = true;
        return CONTEXT;
    }

    public static ThriftClientContext context() {
        if (!CONTEXT.init) {
            throw new ThriftClientException("客户端调用上下文未完成初始化");
        }
        return CONTEXT;
    }

}
