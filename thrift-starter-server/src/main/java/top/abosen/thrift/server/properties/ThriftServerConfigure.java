package top.abosen.thrift.server.properties;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import top.abosen.thrift.common.ServiceSignature;

/**
 * @author qiubaisen
 * @date 2021/9/6
 */
public interface ThriftServerConfigure {

    String configureName();

    /**
     * 从服务标示信息中生成签名key
     *
     * @param serviceSignature 服务标示信息
     * @return 唯一确认一个服务的签名
     */
    String generateSignature(ServiceSignature serviceSignature);

    /**
     * 根据服务配置生成TServer
     *
     * @param properties 配置项
     * @return TServer
     */
    TServer determineTServer(ThriftServerProperties.Service properties, TProcessor processor);

}

