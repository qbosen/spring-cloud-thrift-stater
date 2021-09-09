package top.abosen.thrift.client.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Slf4j
public class ThriftClientConfigurationSelector extends SpringFactoryImportSelector<EnableThriftClient> {
    @Override
    protected boolean isEnabled() {
        return true;
    }
}
