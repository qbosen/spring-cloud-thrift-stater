package top.abosen.thrift.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.abosen.thrift.common.ServiceMode;

import java.util.Collections;
import java.util.List;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@Data
@ConfigurationProperties(prefix = "spring.thrift.client")
public class ThriftClientProperties {
    String mark = "";
    List<Service> services = Collections.emptyList();

    @Data
    public static class Service {
        String serviceName = "";
        ServiceMode serviceMode = ServiceMode.DEFAULT;
        String packageToScan = "";
    }
}
