package top.abosen.thrift.client.properties;

import top.abosen.thrift.client.pool.ThriftServerNode;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.ServiceSignature;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.util.Optional;

public class ApiThriftClientConfigure extends DefaultThriftClientConfigure {
    public ApiThriftClientConfigure(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient);
    }

    @Override
    public String configureName() {
        return Constants.API_THRIFT_CONFIGURE;
    }

    @Override
    public String generateSignature(ServiceSignature signature) {
        String realServiceName = signature.getServiceName().endsWith("api") ?
                signature.getServiceName() + "-thrift" : signature.getServiceName();
        return String.join("$",
                new String[]{realServiceName, signature.getServiceClass().getName(), String.valueOf(signature.getVersion())});
    }

    @Override
    public ThriftServerNode chooseServerNode(String serviceName) {
        return Optional.ofNullable(loadBalancerClient.choose(serviceName))
                .map(it -> serviceName.endsWith("api") ?
                        new ThriftServerNode(it.getHost(), it.getPort() + 2) :
                        new ThriftServerNode(it.getHost(), it.getPort()))
                .orElse(null);
    }
}