package top.abosen.thrift.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.abosen.thrift.client.pool.PortSelector;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.ServiceMode;

import java.util.Collections;
import java.util.List;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@Data
@ConfigurationProperties(prefix = "spring.cloud.thrift.client")
public class ThriftClientProperties {
    Pool pool = new Pool();
    PortConfigure portSelector = new PortConfigure();
    List<Service> services = Collections.emptyList();

    @Data
    public static class Service {
        String serviceName = "";
        String configure = Constants.DEFAULT_CONFIGURE;
        ServiceMode serviceMode = ServiceMode.DEFAULT;
        String packageToScan = "";
    }

    @Data
    public static class PortConfigure implements PortSelector {
        boolean enabled = false;
        int minPort = 15000;
        int maxPort = 65535;
    }

    @Data
    public static class Pool {
        int retryTimes = 3;
        int connectTimeout = 10000;
        int poolMaxTotalPerKey = 60;
        int poolMaxIdlePerKey = 40;
        int poolMinIdlePerKey = 3;
        long poolMaxWait = 180000;

        /**
         * 池对象创建时时验证是否正常可用
         */
        boolean testOnCreate = true;

        /**
         * 池对象借出时验证是否正常可用
         */
        boolean testOnBorrow = true;


        /**
         * 池对象归还时验证是否正常可用
         */
        boolean testOnReturn = true;

        /**
         * 空闲连接自动被空闲连接回收器
         */
        boolean isTestWhileIdle = true;
    }
}
