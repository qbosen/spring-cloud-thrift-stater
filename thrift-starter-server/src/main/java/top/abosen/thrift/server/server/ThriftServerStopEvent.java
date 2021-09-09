package top.abosen.thrift.server.server;

import org.springframework.context.ApplicationEvent;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */
public class ThriftServerStopEvent extends ApplicationEvent {
    public ThriftServerStopEvent(Object source) {
        super(source);
    }
}
