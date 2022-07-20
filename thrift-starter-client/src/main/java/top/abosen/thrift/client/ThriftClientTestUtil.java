package top.abosen.thrift.client;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.pool.ThriftServerNode;
import top.abosen.thrift.client.properties.DefaultThriftClientConfigure;
import top.abosen.thrift.client.properties.ThriftClientConfigure;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.ServiceMode;
import top.abosen.thrift.common.ServiceSignature;
import top.abosen.thrift.common.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * 提供一个直接调用测试的入口
 *
 * @author qiubaisen
 * @date 2022/7/20
 */
@SuppressWarnings("unchecked")
public class ThriftClientTestUtil {
    /**
     * 根据包装类型获取client类, 无编译器类型检查
     *
     * @param serviceClass 最外层的服务包装类
     * @param serviceName  服务端使用服务名
     * @param host         服务端host
     * @param port         服务端port
     * @param <I>          Iface 类型
     * @return client
     */
    public static <I> I getClient(Class<?> serviceClass, String serviceName, String host, int port) throws Exception {
        return (I) getClientObject(serviceClass, serviceName, host, port);
    }

    public static <I> I getClientByIface(Class<I> ifaceClass, String serviceName, String host, int port) throws Exception {
        Class<?> serviceClass = ifaceClass.getEnclosingClass();
        if (serviceClass == null) {
            throw new ThriftClientException(ifaceClass.getName() + "不是一个合法的Iface接口");
        }
        return getClient(serviceClass, serviceName, host, port);
    }

    public static <I, C extends TServiceClient> I getClientByClient(Class<C> clientClass, String serviceName, String host, int port) throws Exception {
        Class<?> serviceClass = clientClass.getEnclosingClass();
        if (serviceClass == null) {
            throw new ThriftClientException(clientClass.getName() + "不是一个合法的Client类");
        }
        return getClient(serviceClass, serviceName, host, port);
    }

    private static Object getClientObject(Class<?> serviceClass, String serviceName, String host, int port)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<? extends TServiceClient> clientClass = Arrays.stream(serviceClass.getClasses())
                .filter(TServiceClient.class::isAssignableFrom)
                .findAny().map(Utils::<Class<? extends TServiceClient>>cast)
                .orElseThrow(() -> new ThriftClientException("未找到相关的Client定义"));
        Class<?> ifaceClass = Arrays.stream(serviceClass.getClasses())
                .filter(it -> it.isInterface() && it.getName().endsWith("$Iface"))
                .findAny()
                .orElseThrow(() -> new ThriftClientException("未找到相关的Iface定义"));

        Constructor<? extends TServiceClient> constructor = clientClass.getConstructor(TProtocol.class);
        ThriftClientConfigure configure = getClientConfigureByServerNode(host, port);
        ServiceSignature serviceSignature = new ServiceSignature(serviceName, clientClass.getEnclosingClass(), Constants.DEFAULT_VERSION);

        ThriftClientProperties.PortConfigure portSelector = new ThriftClientProperties.PortConfigure();
        TTransport transport = configure.determineTTransport(ServiceMode.DEFAULT, configure.chooseServerNode(serviceSignature.getServiceName()), 30_000, portSelector);
        TProtocol tProtocol = configure.determineTProtocol(transport, configure.generateSignature(serviceSignature));
        final Object target = constructor.newInstance(tProtocol);

        return Proxy.newProxyInstance(clientClass.getClassLoader(), new Class[]{ifaceClass}, (proxy, method, args) -> {
            transport.open();
            try {
                return method.invoke(target, args);
            } finally {
                transport.close();
            }
        });
    }

    private static ThriftClientConfigure getClientConfigureByServerNode(String host, int port) {
        return new DefaultThriftClientConfigure(null) {
            @Override
            public ThriftServerNode chooseServerNode(String serviceName) {
                return new ThriftServerNode(host, port);
            }
        };
    }
}
