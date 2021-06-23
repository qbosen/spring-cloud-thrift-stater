package top.abosen.thrift.server.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import top.abosen.thrift.common.Constants;

import java.lang.annotation.*;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface ThriftService {
    /**
     * service name / bean name, 不填为默认bean name, 不可重复
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * @return 版本号
     */
    double version() default Constants.DEFAULT_VERSION;
}
