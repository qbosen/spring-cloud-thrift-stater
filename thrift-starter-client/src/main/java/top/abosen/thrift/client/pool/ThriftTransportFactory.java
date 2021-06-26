package top.abosen.thrift.client.pool;

import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.common.ServiceMode;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
public class ThriftTransportFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftTransportFactory.class);

    private static final int CONNECT_TIMEOUT = 10;

    public static TTransport determineTTranport(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout) {
        TTransport transport;

        switch (serviceMode) {
            case SIMPLE:
            case THREAD_POOL:
                transport = createTSocket(serviceMode, serverNode, connectTimeout);
                break;

            case NON_BLOCKING:
            case HS_HA:
            case THREADED_SELECTOR:
                transport = createTFramedTransport(serviceMode, serverNode, connectTimeout);
                break;

            default:
                throw new ThriftClientException("Service model is configured in wrong way");
        }

        return transport;
    }

    public static TTransport determineTTranport(ServiceMode serviceMode, ThriftServerNode serverNode) {
        return determineTTranport(serviceMode, serverNode, CONNECT_TIMEOUT);
    }

    private static TTransport createTSocket(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout) {
        TTransport transport = new TSocket(serverNode.getHost(), serverNode.getPort(),
                connectTimeout > 0 ? connectTimeout : CONNECT_TIMEOUT);
        LOGGER.info("Established a new socket transport, service model is {}", serviceMode);
        return transport;
    }

    private static TTransport createTFramedTransport(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout) {
        TTransport transport = new TFastFramedTransport(new TSocket(serverNode.getHost(), serverNode.getPort(),
                connectTimeout > 0 ? connectTimeout : CONNECT_TIMEOUT));
        LOGGER.info("Established a new framed transport, service model is {}", serviceMode);
        return transport;
    }

}
