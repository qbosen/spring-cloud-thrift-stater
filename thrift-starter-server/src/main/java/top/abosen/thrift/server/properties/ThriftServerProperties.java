package top.abosen.thrift.server.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.abosen.thrift.common.ServiceMode;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */
@ConfigurationProperties(prefix = "spring.thrift.server")
@Data
public class ThriftServerProperties {
    /**
     * 服务id
     */
    String id;
    /**
     * 服务端口
     */
    int port;
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
    ServiceMode mode = ServiceMode.DEFAULT;
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
        boolean enabled = false;

        /**
         * 服务注册中心的地址
         */
        String host;
        /**
         * 服务注册中心的端口号(默认8500)
         */
        int port = 8500;
        /**
         * 服务健康检查
         */
        HealthCheck healthCheck;


        @Data
        public static class HealthCheck {
            /**
             * 是否允许健康检查
             */
            boolean enabled = true;
            /**
             * 服务健康检查时间间隔 (默认30s)
             */
            int checkInterval = 30;
            /**
             * 服务健康检查超时时间（默认3m）
             */
            int checkTimeout = 3;
        }
    }


}
