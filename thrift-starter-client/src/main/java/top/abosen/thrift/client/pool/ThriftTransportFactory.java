package top.abosen.thrift.client.pool;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFastFramedTransport;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.common.ServiceMode;
import top.abosen.thrift.common.SocketUtils;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
@Slf4j
public class ThriftTransportFactory {

    private static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    public static TTransport determineTTranport(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout, PortSelector portSelector) {
        TTransport transport;

        switch (serviceMode) {
            case SIMPLE:
            case THREAD_POOL:
                transport = createTSocket(serviceMode, serverNode, connectTimeout, portSelector);
                break;

            case NON_BLOCKING:
            case HS_HA:
            case THREADED_SELECTOR:
                transport = createTFramedTransport(serviceMode, serverNode, connectTimeout, portSelector);
                break;

            default:
                throw new ThriftClientException("Service mode is configured in wrong way");
        }

        return transport;
    }

    public static TTransport determineTTranport(ServiceMode serviceMode, ThriftServerNode serverNode, PortSelector portSelector) {
        return determineTTranport(serviceMode, serverNode, DEFAULT_CONNECT_TIMEOUT, portSelector);
    }

    @SneakyThrows
    private static TTransport createTSocket(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout, PortSelector portSelector) {
        TSocket tSocket = new TSocket(serverNode.getHost(), serverNode.getPort(), connectTimeout > 0 ? connectTimeout : DEFAULT_CONNECT_TIMEOUT);
        tryBindCustomPort(portSelector, tSocket);
        log.debug("Established a new socket transport, service mode is {}", serviceMode);
        return tSocket;
    }

    @SneakyThrows
    private static TTransport createTFramedTransport(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout, PortSelector portSelector) {
        TSocket tSocket = new TSocket(serverNode.getHost(), serverNode.getPort(), connectTimeout > 0 ? connectTimeout : DEFAULT_CONNECT_TIMEOUT);
        tryBindCustomPort(portSelector, tSocket);
        TTransport transport = new TFastFramedTransport(tSocket);
        log.debug("Established a new framed transport, service mode is {}", serviceMode);
        return transport;
    }

    private static void tryBindCustomPort(PortSelector portSelector, TSocket tSocket) {
        if (portSelector.isEnabled()) {
            int i = 0, availableTcpPort = -1;
            boolean bindSuccess = false;

            while (i < 3 && !bindSuccess) {
                availableTcpPort = SocketUtils.findAvailableTcpPort(portSelector.getMinPort(), portSelector.getMaxPort());
                try {
                    tSocket.getSocket().bind(new InetSocketAddress(availableTcpPort));
                    bindSuccess = true;
                } catch (IOException e) {
                    i++;
                    log.warn("Thrift clint bind custom port {} failed {} times...", availableTcpPort, i);
                }
            }

            if (bindSuccess) {
                log.debug("Thrift Client bind custom port {}", availableTcpPort);
            } else {
                log.warn("Thrift Client bind custom port {} failed, use a random port by system", availableTcpPort);
            }
        }
    }

}
