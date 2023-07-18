package top.abosen.thrift.client;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.pool.ThriftServerNode;
import top.abosen.thrift.client.properties.ApiThriftClientConfigure;
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

    public static <I> I getClientByIface(Class<I> ifaceClass, String serviceName, IfaceArgs args) throws Exception {
        Class<?> serviceClass = ifaceClass.getEnclosingClass();
        if (serviceClass == null) {
            throw new ThriftClientException(ifaceClass.getName() + "不是一个合法的Iface接口");
        }
        return (I) getClientObject(serviceClass, serviceName, args);
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
        return getClientObject(serviceClass, serviceName, IfaceArgs.builder().configure(host, port).build());
    }

    private static Object getClientObject(Class<?> serviceClass, String serviceName, IfaceArgs ifaceArgs)
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
        ThriftClientConfigure configure = ifaceArgs.getConfigure();
        ServiceSignature serviceSignature = new ServiceSignature(serviceName, clientClass.getEnclosingClass(), Constants.DEFAULT_VERSION);

        TTransport transport = configure.determineTTransport(ifaceArgs.getMode(), configure.chooseServerNode(serviceSignature.getServiceName()), ifaceArgs.getConnectTimeout(), ifaceArgs.getPortConfigure());
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

    @Builder
    @Getter
    public static class IfaceArgs {
        /**
         * socket超时时间, 默认30s
         */
        @Builder.Default
        private int connectTimeout = 30_000;
        /**
         * 使用的端口范围, 默认随机
         */
        @Builder.Default
        private ThriftClientProperties.PortConfigure portConfigure = new ThriftClientProperties.PortConfigure();
        /**
         * 连接模式
         */
        @Builder.Default
        private ServiceMode mode = ServiceMode.DEFAULT;

        private ThriftClientConfigure configure;


        public static class IfaceArgsBuilder {
            /**
             * 直接通过 ip+端口访问服务, 此时服务名为 xx-thrift 服务; 与 default configure 对应
             *
             * @param host host
             * @param port port
             * @return builder
             */
            public IfaceArgsBuilder configure(String host, int port) {
                this.configure = new DefaultThriftClientConfigure(null) {
                    @Override
                    public ThriftServerNode chooseServerNode(String serviceName) {
                        return new ThriftServerNode(host, port);
                    }
                };
                ;
                return this;
            }

            /**
             * 通过consul访问服务, 此时服务名为xxx-api,使用端口+2; 与 api2thrift configure 对应
             *
             * @param client loadBalancerClient
             * @return builder
             */
            public IfaceArgsBuilder configure(LoadBalancerClient client) {
                this.configure = new ApiThriftClientConfigure(client);
                return this;
            }

            public IfaceArgsBuilder configure(ThriftClientConfigure configure) {
                this.configure = configure;
                return this;
            }

        }
    }
}
