package top.abosen.thrift.client.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.properties.ThriftClientConfigure;
import top.abosen.thrift.client.properties.ThriftClientProperties;

import java.util.Objects;
import java.util.Optional;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
@Slf4j
@RequiredArgsConstructor
public class TransportKeyedPooledObjectFactory extends BaseKeyedPooledObjectFactory<ThriftClientKey, TTransport> {

    final ThriftClientConfigure clientConfigure;
    final ThriftClientProperties properties;

    @Override
    public TTransport create(ThriftClientKey key) throws Exception {
        ThriftServerNode node = key.getNode();
        if (StringUtils.isBlank(node.getHost())) {
            throw new ThriftClientException("Invalid Thrift server, node IP address: " + node.getHost());
        }
        if (node.getPort() <= 0 || node.getPort() > 65535) {
            throw new ThriftClientException("Invalid Thrift server, node port: " + node.getPort());
        }

        ThriftClientProperties.Service serviceConfig = properties.getServices().stream()
                .filter(it -> it.getServiceName().equals(key.getServiceName()))
                .findAny()
                .orElseThrow(() -> new ThriftClientException(String.format("无法找到服务[%s]对应的客户端配置:", key.getServiceName())));


        int connectTimeout = Optional.ofNullable(properties.getPool())
                .map(ThriftClientProperties.Pool::getConnectTimeout)
                .filter(it -> it > 0).orElse(30);

        TTransport transport = clientConfigure.determineTTransport(serviceConfig.getServiceMode(), node, connectTimeout);

        try {
            transport.open();
            log.debug("Open a new transport {}", transport);
        } catch (TTransportException e) {
            throw new ThriftClientException("Connect to " + node.getHost() + ":" + node.getPort() + " failed", e);
        }

        return transport;
    }

    @Override
    public PooledObject<TTransport> wrap(TTransport value) {
        return new DefaultPooledObject<>(value);
    }

    @Override
    public boolean validateObject(ThriftClientKey key, PooledObject<TTransport> value) {
        if (Objects.isNull(value)) {
            log.warn("PooledObject is already null");
            return false;
        }

        TTransport transport = value.getObject();
        if (Objects.isNull(transport)) {
            log.warn("Pooled transport is already null");
            return false;
        }

        try {
            return transport.isOpen();
        } catch (Exception e) {
            log.error(e.getCause().getMessage());
            return false;
        }
    }

    @Override
    public void destroyObject(ThriftClientKey key, PooledObject<TTransport> value) throws Exception {
        if (Objects.nonNull(value)) {
            TTransport transport = value.getObject();
            if (Objects.nonNull(transport)) {
                transport.close();
            }
            value.markAbandoned();
        }
    }
}
