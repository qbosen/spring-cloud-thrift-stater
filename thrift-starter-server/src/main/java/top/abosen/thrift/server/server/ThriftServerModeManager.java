package top.abosen.thrift.server.server;

import lombok.SneakyThrows;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.*;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import top.abosen.thrift.server.properties.ThriftServerProperties;
import top.abosen.thrift.server.wrapper.ThriftServiceWrapper;

import java.lang.reflect.Constructor;
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
    public static TServer createServerWithMode(ThriftServerProperties properties, List<ThriftServiceWrapper> serviceWrappers) {
        TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();
        for (ThriftServiceWrapper serviceWrapper : serviceWrappers) {
            Object bean = serviceWrapper.getTarget();
            Class<?> ifaceClass = serviceWrapper.getIfaceType();

            if (Objects.isNull(ifaceClass)) {
                ifaceClass = Stream.of(ClassUtils.getAllInterfaces(bean))
                        .filter(clazz -> clazz.getName().endsWith("$Iface"))
                        .filter(iFace -> iFace.getDeclaringClass() != null)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No thrift IFace found on implementation"));
            }

            @SuppressWarnings("unchecked")
            Class<TProcessor> processorClass = Stream.of(ifaceClass.getDeclaringClass().getDeclaredClasses())
                    .filter(clazz -> clazz.getName().endsWith("$Processor"))
                    .filter(TProcessor.class::isAssignableFrom)
                    .findFirst()
                    .map(processor -> (Class<TProcessor>) processor)
                    .orElseThrow(() -> new IllegalStateException("No thrift IFace found on implementation"));

            Constructor<TProcessor> processorConstructor = processorClass.getConstructor(ifaceClass);

            TProcessor singleProcessor = BeanUtils.instantiateClass(processorConstructor, bean);
            String serviceSignature = serviceWrapper.getSignature();
            multiplexedProcessor.registerProcessor(serviceSignature, singleProcessor);
        }

        switch (properties.getServiceMode()) {
            case SIMPLE: {
                TServerSocket serverSocket = new TServerSocket(properties.getServicePort());
                TServer.Args args = new TSimpleServer.Args(serverSocket)
                        .transportFactory(new TFastFramedTransport.Factory())
                        .protocolFactory(new TCompactProtocol.Factory())
                        .processor(multiplexedProcessor);
                return new TSimpleServer(args);
            }
            case NON_BLOCKING: {
                TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(properties.getServicePort());
                TNonblockingServer.Args args = new TNonblockingServer.Args(serverSocket)
                        .transportFactory(new TFastFramedTransport.Factory())
                        .protocolFactory(new TCompactProtocol.Factory())
                        .processor(multiplexedProcessor);
                return new TNonblockingServer(args);
            }
            case THREAD_POOL: {
                ThriftServerProperties.ThreadPool poolConfig = properties.getThreadPool();
                TServerSocket serverSocket = new TServerSocket(properties.getServicePort());
                TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverSocket)
                        .transportFactory(new TFastFramedTransport.Factory())
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
                        .processor(multiplexedProcessor);
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
                        .processor(multiplexedProcessor);
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
                        .processor(multiplexedProcessor);
                return new TThreadedSelectorServer(args);
            }
            default:
                throw new IllegalStateException("Unexpected value: " + properties.getServiceMode());
        }
    }
}
