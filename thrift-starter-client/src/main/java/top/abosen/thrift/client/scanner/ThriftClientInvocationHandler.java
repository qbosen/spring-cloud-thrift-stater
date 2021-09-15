package top.abosen.thrift.client.scanner;

import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import top.abosen.thrift.client.ThriftClientContext;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.pool.ThriftClientKey;
import top.abosen.thrift.client.pool.ThriftServerNode;
import top.abosen.thrift.client.pool.TransportKeyedObjectPool;
import top.abosen.thrift.client.properties.ThriftClientConfigure;
import top.abosen.thrift.client.properties.ThriftClientConfigureWrapper;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.common.ServiceSignature;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static java.util.Objects.isNull;

/**
 * @author qiubaisen
 * @date 2021/6/25
 */
@Slf4j
public class ThriftClientInvocationHandler implements InvocationHandler {
    final ServiceSignature serviceSignature;
    final Constructor<? extends TServiceClient> clientConstructor;
    final ThriftClientProperties.Service serviceConfig;

    // 延迟缓存属性
    ThriftClientProperties.Pool poolConfig;
    TransportKeyedObjectPool transportPool;
    ThriftClientConfigureWrapper clientConfigureWrapper;

    public ThriftClientInvocationHandler(
            ServiceSignature serviceSignature,
            Constructor<? extends TServiceClient> clientConstructor,
            ThriftClientProperties.Service serviceConfig
    ) {
        this.serviceSignature = serviceSignature;
        this.clientConstructor = clientConstructor;
        this.serviceConfig = serviceConfig;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 延迟设置
        if (isNull(clientConfigureWrapper) || isNull(poolConfig) || isNull(transportPool)) {
            ThriftClientContext context = ThriftClientContext.context();
            this.poolConfig = context.getProperties().getPool();
            this.transportPool = context.getObjectPool();
            this.clientConfigureWrapper = context.getClientConfigureWrapper();
        }
        ThriftClientConfigure clientConfigure = clientConfigureWrapper.getConfigure(serviceConfig.getConfigure());

        String signature = clientConfigure.generateSignature(serviceSignature);
        String serviceName = serviceConfig.getServiceName();
        int retryTimes = 0;
        TTransport transport = null;
        ThriftServerNode node = null;
        ThriftClientKey key = null;
        while (true) {
            if (++retryTimes > poolConfig.getRetryTimes()) {
                log.error("[ThriftClient] 所有客户重试端调用均失败, method:{}, signature:{}, retryTimes:{}", method.getName(), signature, retryTimes);
                throw new ThriftClientException("客户端调用失败: " + signature);
            }
            try {
                node = clientConfigure.chooseServerNode(serviceName);
                if (isNull(node)) {
                    log.warn("[LoadBalancer] 未找到可用的thrift服务: [{}]", serviceName);
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("[LoadBalancer] 获取负载服务节点: [host:{}, port:{}]", node.getHost(), node.getPort());
                }

                key = new ThriftClientKey(signature, serviceName,serviceConfig.getConfigure(), node.getHost(), node.getPort());
                transport = transportPool.borrowObject(key);
                TProtocol protocol = clientConfigure.determineTProtocol(transport, signature);
                Object client = clientConstructor.newInstance(protocol);
                return method.invoke(client, args);

            } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | SecurityException | NoSuchMethodException e) {
                throw new ThriftClientException("无法创建thrift客户端链接", e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException instanceof TTransportException) {
                    TTransportException innerException = (TTransportException) targetException;
                    Throwable realException = innerException.getCause();
                    if (realException instanceof SocketTimeoutException) {
                        if (transport != null) {
                            transport.close();
                        }
                        log.error("[ThriftClient] 请求超时, server: [host:{},port:{}]", node.getHost(), node.getPort());
                        // 超时 直接抛出，不进行重试
                        throw new ThriftClientException("Thrift client request timeout", e);

                    } else if (realException == null && innerException.getType() == TTransportException.END_OF_FILE) {
                        // 服务端直接抛出了异常 or 服务端在被调用的过程中被关闭了
                        // 重试
                        transportPool.clear(key); // 把以前的对象池进行销毁
                        if (transport != null) {
                            transport.close();
                        }

                    } else if (realException instanceof SocketException) {
                        // 重试
                        transportPool.clear(key);
                        if (transport != null) {
                            transport.close();
                        }
                    }

                } else if (targetException instanceof TApplicationException) {  // 有可能服务端返回的结果里存在null
                    log.error("ThriftServer异常: [signature:{}]", signature);
                    transportPool.clear(key);
                    if (transport != null) {
                        transport.close();
                    }
                    // 服务端的业务异常，不重试
                    throw new ThriftClientException("服务端调用失败: " + targetException.getMessage());

                } else if (targetException instanceof TException) { // 自定义异常
                    throw targetException;
                } else {
                    // Unknown Exception
                    throw e;
                }

            } catch (Exception e) {
                log.error("[ThriftClient] 调用失败", e);
                throw e;
            } finally {
                try {
                    if (transportPool != null && transport != null) {
                        transportPool.returnObject(key, transport);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
