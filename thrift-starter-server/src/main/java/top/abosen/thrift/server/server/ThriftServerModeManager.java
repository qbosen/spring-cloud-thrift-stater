package top.abosen.thrift.server.server;

import lombok.SneakyThrows;
import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.*;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import top.abosen.thrift.common.ServiceSignature;
import top.abosen.thrift.server.exception.ThriftServerException;
import top.abosen.thrift.server.exception.ThriftServerExceptionConverter;
import top.abosen.thrift.server.properties.ThriftServerConfigure;
import top.abosen.thrift.server.properties.ThriftServerProperties;
import top.abosen.thrift.server.wrapper.ThriftServiceWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author qiubaisen
 * @date 2021/6/17
 */
public class ThriftServerModeManager {
    @SneakyThrows
    public static TServer createServerWithMode(
            ThriftServerProperties.Service properties, ThriftServerConfigure serviceConfigure,
            List<ThriftServiceWrapper> serviceWrappers, ThriftServerExceptionConverter exceptionConverter) {
        TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();
        for (ThriftServiceWrapper serviceWrapper : serviceWrappers) {
            Object target = serviceWrapper.getTarget();
            Class<?> ifaceClass = serviceWrapper.getIfaceType();

            if (Objects.isNull(ifaceClass)) {
                ifaceClass = Stream.of(ClassUtils.getAllInterfaces(target))
                        .filter(clazz -> clazz.getName().endsWith("$Iface"))
                        .filter(iFace -> iFace.getDeclaringClass() != null)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No thrift IFace found on implementation"));
            }
            final Class<?> finalIface = ifaceClass;

            @SuppressWarnings("unchecked")
            Class<TProcessor> processorClass = Stream.of(finalIface.getDeclaringClass().getDeclaredClasses())
                    .filter(clazz -> clazz.getName().endsWith("$Processor"))
                    .filter(TProcessor.class::isAssignableFrom)
                    .findFirst()
                    .map(processor -> (Class<TProcessor>) processor)
                    .orElseThrow(() -> new IllegalStateException("No thrift IFace found on implementation"));

            Constructor<TProcessor> processorConstructor = processorClass.getConstructor(finalIface);

            // 异常包装
            Object wrapTarget = Proxy.newProxyInstance(finalIface.getClassLoader(), new Class[]{finalIface}, (proxy, method, args) -> {
                try {
                    return method.invoke(target, args);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    throw new ThriftServerException("服务端调用失败", e);
                } catch (InvocationTargetException invocationTargetException) {
                    Throwable e = invocationTargetException.getTargetException();
                    // 业务上对异常进行统一处理
                    e = exceptionConverter.convert(e);
                    if (TException.class.isAssignableFrom(e.getClass())) {
                        throw e;
                    }
                    // 会被 ProcessFunction 捕获并转换为 TApplicationException 再被客户端处理
                    throw new TException(e);
                }
            });

            TProcessor singleProcessor = BeanUtils.instantiateClass(processorConstructor, wrapTarget);
            ServiceSignature serviceSignature = serviceWrapper.serviceSignature(properties.getServiceName());
            multiplexedProcessor.registerProcessor(serviceConfigure.generateSignature(serviceSignature), singleProcessor);
        }

        return serviceConfigure.determineTServer(properties, multiplexedProcessor);
    }

    /**
     * 默认的 TServer 构造方式
     */
    @SneakyThrows
    public static TServer determineTServer(ThriftServerProperties.Service properties, TProcessor processor) {
        switch (properties.getServiceMode()) {
            case SIMPLE: {
                TServerSocket serverSocket = new TServerSocket(properties.getServicePort());
                TServer.Args args = new TSimpleServer.Args(serverSocket)
                        .transportFactory(new TTransportFactory())
                        .protocolFactory(new TCompactProtocol.Factory())
                        .processor(processor);
                return new TSimpleServer(args);
            }
            case NON_BLOCKING: {
                TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(properties.getServicePort());
                TNonblockingServer.Args args = new TNonblockingServer.Args(serverSocket)
                        .transportFactory(new TFastFramedTransport.Factory())
                        .protocolFactory(new TCompactProtocol.Factory())
                        .processor(processor);
                return new TNonblockingServer(args);
            }
            case THREAD_POOL: {
                ThriftServerProperties.ThreadPool poolConfig = properties.getThreadPool();
                TServerSocket serverSocket = new TServerSocket(properties.getServicePort());
                TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverSocket)
                        .transportFactory(new TTransportFactory())
                        .protocolFactory(new TCompactProtocol.Factory())
                        .minWorkerThreads(poolConfig.getMinWorkerThreads())
                        .maxWorkerThreads(poolConfig.getMaxWorkerThreads())
                        .requestTimeout(poolConfig.getRequestTimeout())
                        .executorService(new ThreadPoolExecutor(
                                poolConfig.getMinWorkerThreads(),
                                poolConfig.getMaxWorkerThreads(),
                                poolConfig.getKeepAliveTime(),
                                TimeUnit.SECONDS,
                                new LinkedBlockingDeque<>(properties.getQueueSize())))
                        .processor(processor);
                return new TThreadPoolServer(args);
            }
            case HS_HA: {
                ThriftServerProperties.HsHa hsHaConfig = properties.getHsHa();
                TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(properties.getServicePort());
                THsHaServer.Args args = new THsHaServer.Args(serverSocket)
                        .transportFactory(new TFastFramedTransport.Factory())
                        .protocolFactory(new TCompactProtocol.Factory())
                        .minWorkerThreads(hsHaConfig.getMinWorkerThreads())
                        .maxWorkerThreads(hsHaConfig.getMaxWorkerThreads())
                        .executorService(new ThreadPoolExecutor(
                                hsHaConfig.getMinWorkerThreads(),
                                hsHaConfig.getMaxWorkerThreads(),
                                hsHaConfig.getKeepAliveTime(),
                                TimeUnit.SECONDS,
                                new LinkedBlockingDeque<>(properties.getQueueSize())))
                        .processor(processor);
                return new THsHaServer(args);
            }
            case THREADED_SELECTOR: {
                ThriftServerProperties.ThreadedSelector selectorConfig = properties.getThreadedSelector();
                TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(properties.getServicePort());
                TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(serverSocket)
                        .transportFactory(new TFastFramedTransport.Factory())
                        .protocolFactory(new TCompactProtocol.Factory())
                        .selectorThreads(selectorConfig.getSelectorThreads())
                        .workerThreads(selectorConfig.getMinWorkerThreads())
                        .acceptQueueSizePerThread(selectorConfig.getAcceptQueueSizePerThread())
                        .executorService(new ThreadPoolExecutor(
                                selectorConfig.getMinWorkerThreads(),
                                selectorConfig.getMaxWorkerThreads(),
                                selectorConfig.getKeepAliveTime(),
                                TimeUnit.SECONDS,
                                new LinkedBlockingDeque<>(properties.getQueueSize())))
                        .processor(processor);
                return new TThreadedSelectorServer(args);
            }
            default:
                throw new IllegalStateException("Unexpected value: " + properties.getServiceMode());
        }
    }
}
