package top.abosen.thrift.server.server;

import org.springframework.context.ApplicationEvent;
import top.abosen.thrift.server.properties.ThriftServerProperties;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
public class ThriftServerStopEvent extends ApplicationEvent {
    public ThriftServerStopEvent(Object source) {
        super(source);
    }
}
