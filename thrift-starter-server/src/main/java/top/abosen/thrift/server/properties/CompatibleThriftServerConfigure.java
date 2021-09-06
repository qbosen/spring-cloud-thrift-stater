package top.abosen.thrift.server.properties;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import top.abosen.thrift.common.ServiceSignature;
import top.abosen.thrift.server.server.ThriftServerModeManager;

/**
 * 兼容模式
 *
 * @author qiubaisen
 * @date 2021/9/6
 */
public class CompatibleThriftServerConfigure implements ThriftServerConfigure {
    @Override public String generateSignature(ServiceSignature signature) {
        return String.join("$",
                new String[]{signature.getServiceName(), signature.getServiceClass().getName(), String.valueOf(signature.getVersion())});

    }

    @Override public TServer determineTServer(ThriftServerProperties.Service properties, TProcessor processor) {
        return ThriftServerModeManager.determineTServer(properties, processor);
    }
}
