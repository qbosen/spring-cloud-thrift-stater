package top.abosen.thrift.server.server;

import org.springframework.context.ApplicationEvent;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
public class ThriftServerStartEvent extends ApplicationEvent {
    public ThriftServerStartEvent(Object source) {
        super(source);
    }
}
