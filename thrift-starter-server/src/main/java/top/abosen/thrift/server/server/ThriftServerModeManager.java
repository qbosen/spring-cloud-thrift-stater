package top.abosen.thrift.server.server;

import lombok.SneakyThrows;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import top.abosen.thrift.server.properties.ThriftServerProperties;
import top.abosen.thrift.server.wrapper.ThriftServiceWrapper;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author qiubaisen
 * @date 2021/6/17
 */
public class ThriftServerModeManager {
    @SneakyThrows
    public static TServer createServerWithMode(ThriftServerProperties properties, List<ThriftServiceWrapper> serviceWrappers) {
        TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();
        // todo 优化
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
            // todo 兼容老版本，如果配置了兼容属性，从相同服务中选取最新版本做兼容注册
            multiplexedProcessor.registerProcessor(serviceSignature, singleProcessor);
        }
        TServerSocket serverSocket = new TServerSocket(properties.getPort());
        TServer.Args args = new TSimpleServer.Args(serverSocket)
                .transportFactory(new TFastFramedTransport.Factory())
                .protocolFactory(new TCompactProtocol.Factory())
                .processor(multiplexedProcessor);
        return new TSimpleServer(args);
    }
}
