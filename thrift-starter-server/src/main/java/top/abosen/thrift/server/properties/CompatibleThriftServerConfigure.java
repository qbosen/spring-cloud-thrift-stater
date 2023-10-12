package top.abosen.thrift.server.properties;

import lombok.SneakyThrows;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportFactory;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.ServiceMode;
import top.abosen.thrift.common.ServiceSignature;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 兼容模式
 *
 * @author qiubaisen
 * @date 2021/9/6
 */
public class CompatibleThriftServerConfigure implements ThriftServerConfigure {
    @Override public String configureName() {
        return Constants.COMPATIBLE_CONFIGURE;
    }

    @Override public String generateSignature(ServiceSignature signature) {
        /*签名方式使用 `serviceClass.getSimpleName()` 存在重复的风险*/
        return signature.getServiceClass().getSimpleName();
    }

    @SneakyThrows
    @Override public TServer determineTServer(ThriftServerProperties.Service properties, TProcessor processor) {
        /*使用 TTransportFactory 而非 TFastFramedTransport.Factory*/
        /*使用 TBinaryProtocol.Factory 而非 TCompactProtocol.Factory*/
        ThriftServerProperties.ThreadPool poolConfig = properties.getThreadPool();
        TServerSocket serverSocket = new TServerSocket(properties.getServicePort());
        TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverSocket)
                .transportFactory(new TTransportFactory())
                .protocolFactory(new TBinaryProtocol.Factory(true, true))
                .minWorkerThreads(poolConfig.getMinWorkerThreads())
                .maxWorkerThreads(poolConfig.getMaxWorkerThreads())
                .executorService(new ThreadPoolExecutor(
                        poolConfig.getMinWorkerThreads(),
                        poolConfig.getMaxWorkerThreads(),
                        poolConfig.getKeepAliveTime(),
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<>(properties.getQueueSize())))
                .processor(processor);
        return new TThreadPoolServer(args);
    }
}
