package top.abosen.thrift.client.scanner;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.common.signature.DefaultServiceSignatureGenerator;
import top.abosen.thrift.common.signature.ServiceSignature;
import top.abosen.thrift.common.signature.ServiceSignatureGenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author qiubaisen
 * @date 2021/6/25
 */
public class ThriftClientInvocationHandler implements InvocationHandler {
    private final DiscoveryClient discoveryClient;
    private ServiceSignature serviceSignature;
    private Class<?> clientClass;
    private Constructor<? extends TServiceClient> clientConstructor;
    private ThriftClientProperties.Service serviceConfig;
    private ServiceSignatureGenerator signatureGenerator = new DefaultServiceSignatureGenerator();

    public ThriftClientInvocationHandler(ServiceSignature serviceSignature, Class<?> clientClass, Constructor<? extends TServiceClient> clientConstructor, ThriftClientProperties.Service serviceConfig, DiscoveryClient discoveryClient) {
        this.serviceSignature = serviceSignature;
        this.clientClass = clientClass;
        this.clientConstructor = clientConstructor;
        this.serviceConfig = serviceConfig;
        this.discoveryClient = discoveryClient;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceConfig.getServiceName());
        ServiceInstance serviceInstance = instances.get(0);
        TTransport transport = new TSocket(serviceInstance.getHost(), serviceInstance.getPort());
        TProtocol protocol = new TCompactProtocol(new TFastFramedTransport(transport));
        TMultiplexedProtocol multiplexedProtocol = new TMultiplexedProtocol(protocol, signatureGenerator.generate(serviceSignature));
        Object client = clientConstructor.newInstance(multiplexedProtocol);

        return method.invoke(client, args);
    }
}
