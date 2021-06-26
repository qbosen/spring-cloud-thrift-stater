package top.abosen.thrift.server.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.abosen.thrift.common.ServiceMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */
@ConfigurationProperties(prefix = "spring.cloud.thrift.server")
@Data
public class ThriftServerProperties {
    /**
     * 服务id
     */
    String serviceName;
    /**
     * 服务端口
     */
    int servicePort;
    /**
     * 服务的工作线程队列容量
     */
    int queueSize = 1000;
    /**
     * 服务模型(单线程/多线程/阻塞/非阻塞) 默认hsHa
     * <p>
     * simple: 单线程阻塞模型
     * nonBlocking: 单线程非阻塞模型
     * threadPool: 线程池同步模型
     * hsHa: 半同步半异步模型
     * threadedSelector: 线程池选择器模型
     * </p>
     */
    ServiceMode serviceMode = ServiceMode.DEFAULT;
    /**
     * 服务注册信息（默认不开启）
     */
    Discovery discovery;

    // todo add hsHa/threadPool/threadedSelector configuration
    @Data
    public static class Discovery {
        /**
         * 是否允许服务注册
         */
        boolean register = true;
        boolean healthCheck = true;
        boolean preferIpAddress = true;
        String instanceId;
        List<String> tags = new ArrayList<>();
    }


}
