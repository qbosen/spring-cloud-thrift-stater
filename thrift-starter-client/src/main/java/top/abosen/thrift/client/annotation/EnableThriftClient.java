package top.abosen.thrift.client.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ThriftClientConfigurationSelector.class)
public @interface EnableThriftClient {
}
