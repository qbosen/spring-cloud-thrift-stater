package top.abosen.thrift.client.pool;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
public class TransportKeyedObjectPool extends GenericKeyedObjectPool<ThriftClientKey, TTransport> {

    public TransportKeyedObjectPool(KeyedPooledObjectFactory<ThriftClientKey, TTransport> factory) {
        super(factory);
    }

    public TransportKeyedObjectPool(KeyedPooledObjectFactory<ThriftClientKey, TTransport> factory, GenericKeyedObjectPoolConfig<TTransport> config) {
        super(factory, config);
    }

    @Override
    public TTransport borrowObject(ThriftClientKey key) throws Exception {
        return super.borrowObject(key);
    }

    @Override
    public void returnObject(ThriftClientKey key, TTransport obj) {
        super.returnObject(key, obj);
    }
}
