package top.abosen.thrift.server.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ThriftServerConfigurationSelector.class)
public @interface EnableThriftServer {
}
