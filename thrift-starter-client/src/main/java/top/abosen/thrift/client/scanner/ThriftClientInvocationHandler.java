package top.abosen.thrift.client.scanner;

import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.springframework.cloud.client.ServiceInstance;
import top.abosen.thrift.client.ThriftClientContext;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.common.signature.DefaultServiceSignatureGenerator;
import top.abosen.thrift.common.signature.ServiceSignature;
import top.abosen.thrift.common.signature.ServiceSignatureGenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author qiubaisen
 * @date 2021/6/25
 */
@Slf4j
public class ThriftClientInvocationHandler implements InvocationHandler {
    private ServiceSignature serviceSignature;
    private Class<?> clientClass;
    private Constructor<? extends TServiceClient> clientConstructor;
    private ThriftClientProperties.Service serviceConfig;
    private ServiceSignatureGenerator signatureGenerator = new DefaultServiceSignatureGenerator();

    public ThriftClientInvocationHandler(ServiceSignature serviceSignature, Class<?> clientClass,
                                         Constructor<? extends TServiceClient> clientConstructor, ThriftClientProperties.Service serviceConfig) {
        this.serviceSignature = serviceSignature;
        this.clientClass = clientClass;
        this.clientConstructor = clientConstructor;
        this.serviceConfig = serviceConfig;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ServiceInstance serviceInstance = ThriftClientContext.context().getLoadBalancerClient().choose(serviceConfig.getServiceName());
        log.debug("负载调用:{}:{}", serviceInstance.getHost(), serviceInstance.getPort());
        TTransport transport = new TFastFramedTransport(new TSocket(serviceInstance.getHost(), serviceInstance.getPort()));
        TProtocol protocol = new TCompactProtocol(transport);
        TMultiplexedProtocol multiplexedProtocol = new TMultiplexedProtocol(protocol, signatureGenerator.generate(serviceSignature));
        Object client = clientConstructor.newInstance(multiplexedProtocol);
        try {
            transport.open();
            return method.invoke(client, args);
        } finally {
            transport.close();
        }
    }
}
