package top.abosen.thrift.client.properties;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import top.abosen.thrift.client.pool.PortSelector;
import top.abosen.thrift.client.pool.ThriftServerNode;
import top.abosen.thrift.client.pool.ThriftTransportFactory;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.ServiceMode;
import top.abosen.thrift.common.ServiceSignature;

import java.util.Optional;

/**
 * 兼容老的服务端
 *
 * @author qiubaisen
 * @date 2021/9/3
 */

@RequiredArgsConstructor
public class CompatibleThriftClientConfigure implements ThriftClientConfigure {
    protected final LoadBalancerClient loadBalancerClient;

    @Override public String configureName() {
        return Constants.COMPATIBLE_CONFIGURE;
    }

    @Override public String generateSignature(ServiceSignature signature) {
        /*签名方式使用 `serviceClass.getSimpleName()` 存在重复的风险*/
        return signature.getServiceClass().getSimpleName();
    }

    /**
     * 默认从服务发现获取
     */
    @Override public ThriftServerNode chooseServerNode(String serviceName) {
        /*服务是如果以 `api` 结尾的服务，则端口`+1`*/
        return Optional.ofNullable(loadBalancerClient.choose(serviceName))
                .map(it -> serviceName.endsWith("api") ?
                        new ThriftServerNode(it.getHost(), it.getPort() + 1) :
                        new ThriftServerNode(it.getHost(), it.getPort()))
                .orElse(null);
    }

    @Override public TTransport determineTTransport(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout, PortSelector portSelector) {
        /*服务端模式为 `thread_pool`*/
        return ThriftTransportFactory.determineTTranport(ServiceMode.THREAD_POOL, serverNode, connectTimeout, portSelector);
    }

    @Override public TProtocol determineTProtocol(TTransport transport, String signature) {
        /*传输协议使用 `TBinaryProtocal` 性能更低*/
        return new TMultiplexedProtocol(new TBinaryProtocol(transport), signature);
    }

}
