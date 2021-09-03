package top.abosen.thrift.client.properties;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import top.abosen.thrift.client.pool.ThriftServerNode;
import top.abosen.thrift.client.pool.ThriftTransportFactory;
import top.abosen.thrift.common.ServiceMode;
import top.abosen.thrift.common.ServiceSignature;

import java.util.Optional;

/**
 * @author qiubaisen
 * @date 2021/9/3
 */

@RequiredArgsConstructor
public class DefaultThriftClientConfigure implements ThriftClientConfigure {
    protected final LoadBalancerClient loadBalancerClient;

    @Override public String generateSignature(ServiceSignature signature) {
        return String.join("$",
                new String[]{signature.getServiceName(), signature.getServiceClass().getName(), String.valueOf(signature.getVersion())});
    }

    /**
     * 默认从服务发现获取
     */
    @Override public ThriftServerNode chooseServerNode(String serviceName) {
        return Optional.ofNullable(loadBalancerClient.choose(serviceName))
                .map(it -> new ThriftServerNode(it.getHost(), it.getPort()))
                .orElse(null);
    }

    @Override public TTransport determineTTransport(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout) {
        return ThriftTransportFactory.determineTTranport(serviceMode, serverNode, connectTimeout);
    }

    @Override public TProtocol determineTProtocol(TTransport transport, String signature) {
        return new TMultiplexedProtocol(new TCompactProtocol(transport), signature);
    }

}
