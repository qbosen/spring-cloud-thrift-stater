package top.abosen.thrift.client.properties;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import top.abosen.thrift.client.pool.PortSelector;
import top.abosen.thrift.client.pool.ThriftServerNode;
import top.abosen.thrift.common.ServiceMode;
import top.abosen.thrift.common.ServiceSignature;

/**
 * @author qiubaisen
 * @date 2021/9/3
 */
public interface ThriftClientConfigure {

    /**
     * 唯一的clientConfigure名称
     *
     * @return configure 名称
     */
    String configureName();

    /**
     * 从服务标示信息中生成签名key
     *
     * @param serviceSignature 服务标示信息
     * @return 唯一确认一个服务的签名
     */
    String generateSignature(ServiceSignature serviceSignature);

    /**
     * 根据 serviceName 获取服务节点地址
     *
     * @param serviceName 服务名
     * @return 服务节点
     */
    ThriftServerNode chooseServerNode(String serviceName);

    /**
     * 根据 客户端服务模式配置 和 服务节点信息，创建 TTransport，该对象会被池化缓存
     *
     * @param serviceMode    服务模式
     * @param serverNode     服务节点
     * @param connectTimeout 连接超时时间 秒
     * @param portSelector   端口范围配置
     * @return 配置好的 TTransport
     */
    TTransport determineTTransport(ServiceMode serviceMode, ThriftServerNode serverNode, int connectTimeout, PortSelector portSelector);

    /**
     * 配置协议
     *
     * @param transport transport
     * @param signature 签名key
     * @return protocol
     */
    TProtocol determineTProtocol(TTransport transport, String signature);
}
