package top.abosen.thrift.server.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.abosen.thrift.common.Constants;
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
    List<Service> services;

    @Data
    public static class Service {
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
         * 自定义服务配置bean名称
         */
        String configure = Constants.DEFAULT_CONFIGURE;

        /**
         * 数据包大小,超过长度拒绝
         * <p>
         * 可用于排除大部分非thrift IDL定义的数据包,避免OOM
         */
        long maxReadBufferBytes = 1024 * 1024L;

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
         * 服务注册信息
         */
        Discovery discovery = new Discovery();

        ThreadPool threadPool = new ThreadPool();
        HsHa hsHa = new HsHa();
        ThreadedSelector threadedSelector = new ThreadedSelector();
    }

    @Data
    public static class Discovery {
        boolean register = false;
        boolean healthCheck = true;
        boolean preferIpAddress = true;
        String instanceId;
        List<String> tags = new ArrayList<>();
    }

    @Data
    public static class ThreadPool {
        int minWorkerThreads = 5;
        int maxWorkerThreads = 20;
        int requestTimeout = 5;
        int keepAliveTime = 60;
    }

    @Data
    public static class HsHa implements ThreadPoolExecutorConfigure {
        int minWorkerThreads = 5;
        int maxWorkerThreads = 20;
        int keepAliveTime = 60;
        int queueSize = -1;
    }

    @Data
    public static class ThreadedSelector implements ThreadPoolExecutorConfigure {
        int minWorkerThreads = 5;
        int maxWorkerThreads = 20;
        int keepAliveTime = 300;
        int queueSize = -1;
        int selectorThreads = 2;
        int acceptQueueSizePerThread = 4;
        int backlog = 1024;
    }

    public interface ThreadPoolExecutorConfigure {
        int getMinWorkerThreads();

        int getMaxWorkerThreads();

        int getKeepAliveTime();

        /**
         * 小于等于0 表示使用外部 queueSize 配置; 默认使用外部配置
         *
         * @return 队列容量
         */
        int getQueueSize();
    }

}
